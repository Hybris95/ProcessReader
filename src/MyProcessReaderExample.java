import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.Tlhelp32;
import com.sun.jna.platform.win32.WinNT;

//Imports for MyKernel32
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.Pointer;
import com.sun.jna.Memory;

public class MyProcessReaderExample
{
    private static final boolean DEBUG = false;
	static Kernel32 kernel32 = (Kernel32)Native.loadLibrary(Kernel32.class, W32APIOptions.UNICODE_OPTIONS);
	static MyKernel32 myKernel32 = (MyKernel32)Native.loadLibrary("kernel32", MyKernel32.class);
	static String usage = "Usage: java MyProcessReaderExample [processName] [readSize] [readAddress] [readOffset]" + System.getProperty("line.separator") + "processName example: firefox.exe" + System.getProperty("line.separator") + "readSize (in bytes) example : 4" + System.getProperty("line.separator") + "readAddress (hexadecimal) example : 00010000" + System.getProperty("line.separator") + "readOffset (decimal) example : 0";
	
	interface MyKernel32 extends StdCallLibrary
	{
		// Make a homemade ReadProcessMemory (not implemented in 3.5.1 yet)
		boolean WriteProcessMemory(Pointer p, int address, Pointer buffer, int size, IntByReference written);
		boolean ReadProcessMemory(Pointer hProcess, int inBaseAddress, Pointer outputBuffer, int nSize, IntByReference outNumberOfBytesRead);
		Pointer OpenProcess(int desired, boolean inherit, int pid);
		int GetLastError();
	}
	
	public static void main(String[] args)
	{
		if(args.length < 4)
		{
			System.err.println(usage);
			System.exit(1);
		}
		String processName = "";
		int readSize = 0;
		int readAddress = 0;
		int readOffset = 0;
		try{
			processName = args[0];//Defines the name of the process to search for
			readSize = Integer.parseInt(args[1], 10);//Defines the size of the value to read
			readAddress = Integer.parseInt(args[2], 16);//Defines the address of the value to read (hexadecimal)
			readOffset = Integer.parseInt(args[3], 10);//Defines the offset of the value to read
		}
		catch(NumberFormatException e)
		{
			System.err.println(usage);
			System.exit(1);
		}
		
		long processId = FindProcessId(processName);//Searchs for a process id from its name
		if(processId == 0L)//The process was not found
		{
			System.err.println("The searched process was not found : " + processName);
			System.exit(1);
		}
		System.out.println(processName + " id : " + processId);//Shows us the process id
		
		/*
		WinNT.HANDLE openedProcess = kernel32.OpenProcess(kernel32.READ_CONTROL, false, (int)processId);//Open the process with access right (http://msdn.microsoft.com/en-us/library/windows/desktop/ms684880%28v=vs.85%29.aspx)
		if(openedProcess == null)//Could not open the process
		{
			System.err.println("Could not open the process : " + kernel32.GetLastError());
			System.exit(1);
		}
		*/
		
		int rights = 0x0010 + 0x0020 + 0x0008;//kernel32.PROCESS_VM_READ (0x0010), kernel32.PROCESS_VM_WRITE (0x0020), kernel32.VM_OPERATION (0x0008)
		Pointer openedProcess = myKernel32.OpenProcess(rights, true, (int)processId);//Open the process under read access rights
		if(openedProcess == null)//Could not open the process
		{
			System.err.println("Could not open the process : " + myKernel32.GetLastError());
			System.exit(1);
		}
		
		int readValue = 0;
		{
			Memory output = new Memory(readSize);
			if(!myKernel32.ReadProcessMemory(openedProcess, readAddress, output, readSize, new IntByReference(0)))
			{
				int error = myKernel32.GetLastError();
				switch(error)
				{
					default:
						System.err.println("Failed to read the process : " + error);
						break;
					case 0x12B:
						System.err.println("Failed to read the specified address");
						break;
				}
				System.exit(1);
			}
			readValue = output.getInt(readOffset);
			System.out.println(readValue + " has been read at 0x" + args[2] + "+" + readOffset + " on " + readSize + "bytes.");
		}
		
		{// Increase the read value by one
			IntByReference written = new IntByReference(0);
			Memory toWrite = new Memory(readSize);
			toWrite.setInt(readOffset, readValue+1);//This should change if the readSize is different than 4
			if(!myKernel32.WriteProcessMemory(openedProcess, readAddress, toWrite, 1, written))
			{
				int error = myKernel32.GetLastError();
				switch(error)
				{
					default:
						System.err.println("Failed to write in the process : " + error);
						break;
				}
				System.exit(1);
			}
			System.out.println("Wrote " + written.getValue() + " times.");
		}
		
		{// Read again the value
			Memory output = new Memory(readSize);
			if(!myKernel32.ReadProcessMemory(openedProcess, readAddress, output, readSize, new IntByReference(0)))
			{
				int error = myKernel32.GetLastError();
				switch(error)
				{
					default:
						System.err.println("Failed to read the process : " + error);
						break;
					case 0x12B:
						System.err.println("Failed to read the specified address");
						break;
				}
				System.exit(1);
			}
			readValue = output.getInt(readOffset);
			System.out.println(readValue + " has been read at 0x" + args[2] + "+" + readOffset + " on " + readSize + "bytes.");
		}
		
		{// Restore the read value to its original state
			IntByReference written = new IntByReference(0);
			Memory toWrite = new Memory(readSize);
			toWrite.setInt(readOffset, readValue-1);//This should change if the readSize is different than 4
			if(!myKernel32.WriteProcessMemory(openedProcess, readAddress, toWrite, 1, written))
			{
				int error = myKernel32.GetLastError();
				switch(error)
				{
					default:
						System.err.println("Failed to write in the process : " + error);
						break;
				}
				System.exit(1);
			}
			System.out.println("Wrote " + written.getValue() + " times.");
		}
		
		{// Read again the value
			Memory output = new Memory(readSize);
			if(!myKernel32.ReadProcessMemory(openedProcess, readAddress, output, readSize, new IntByReference(0)))
			{
				int error = myKernel32.GetLastError();
				switch(error)
				{
					default:
						System.err.println("Failed to read the process : " + error);
						break;
					case 0x12B:
						System.err.println("Failed to read the specified address");
						break;
				}
				System.exit(1);
			}
			readValue = output.getInt(readOffset);
			System.out.println(readValue + " has been read at 0x" + args[2] + "+" + readOffset + " on " + readSize + "bytes.");
		}
	}
	
	static long FindProcessId(String processName)
	{
		// This Reference will contain the processInfo that will be parsed to recover the ProcessId
		Tlhelp32.PROCESSENTRY32.ByReference processInfo = new Tlhelp32.PROCESSENTRY32.ByReference();
		
		// This Handle allows us to parse the process map
		WinNT.HANDLE processesSnapshot = kernel32.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new DWORD(0L));
		if(processesSnapshot == kernel32.INVALID_HANDLE_VALUE)
		{
			if(DEBUG) System.err.println("INVALID_HANDLE_VALUE");
			return 0L;
		}
		
		try{// This will parse all the processes to find the process id corresponding to the process name
			kernel32.Process32First(processesSnapshot, processInfo);
			if(processName.equals(Native.toString(processInfo.szExeFile)))
			{
				if(DEBUG) System.out.println("Process " + processName + " found : " + processInfo.th32ProcessID.longValue());
				return processInfo.th32ProcessID.longValue();
			}
			
			while(kernel32.Process32Next(processesSnapshot, processInfo))
			{
				if(processName.equals(Native.toString(processInfo.szExeFile)))
				{
					if(DEBUG) System.out.println("Process " + processName + " found : " + processInfo.th32ProcessID.longValue());
					return processInfo.th32ProcessID.longValue();
				}
			}
			
			if(DEBUG) System.out.println("Did not found the requested Process: " + processName);
			return 0L;
		}
		finally
		{
			kernel32.CloseHandle(processesSnapshot);
		}
	}
}