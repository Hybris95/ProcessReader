package com.hybris95;

import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.Tlhelp32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.Win32Exception;

import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.Pointer;
import com.sun.jna.Memory;

public class ProcessAccess
{
	private static Kernel32 kernel32 = (Kernel32)Native.loadLibrary(Kernel32.class, W32APIOptions.UNICODE_OPTIONS);
	private static int PROCESS_VM_READ = 0x0010;
	private static int PROCESS_VM_WRITE = 0x0020;
	private static int VM_OPERATION = 0x0008;
	
	private static long FindProcessId(String processName) throws Win32Exception
	{
		// This Reference will contain the processInfo that will be parsed to recover the ProcessId
		Tlhelp32.PROCESSENTRY32.ByReference processInfo = new Tlhelp32.PROCESSENTRY32.ByReference();
		
		// This Handle allows us to parse the process map
		WinNT.HANDLE processesSnapshot = kernel32.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new DWORD(0L));
		if(processesSnapshot == kernel32.INVALID_HANDLE_VALUE)
		{
			throw new Win32Exception(1);
		}
		
		try{// This will parse all the processes to find the process id corresponding to the process name
			kernel32.Process32First(processesSnapshot, processInfo);
			if(processName.equals(Native.toString(processInfo.szExeFile)))
			{
				return processInfo.th32ProcessID.longValue();
			}
			
			while(kernel32.Process32Next(processesSnapshot, processInfo))
			{
				if(processName.equals(Native.toString(processInfo.szExeFile)))
				{
					return processInfo.th32ProcessID.longValue();
				}
			}
			throw new Win32Exception(1);
		}
		finally
		{
			kernel32.CloseHandle(processesSnapshot);
		}
	}
	
	interface MyKernel32 extends StdCallLibrary
	{
		boolean WriteProcessMemory(Pointer p, int address, Pointer buffer, int size, IntByReference written);
		boolean ReadProcessMemory(Pointer hProcess, int inBaseAddress, Pointer outputBuffer, int nSize, IntByReference outNumberOfBytesRead);
		Pointer OpenProcess(int desired, boolean inherit, int pid);
		int GetLastError();
	}
	
	private MyKernel32 myKernel32 = (MyKernel32)Native.loadLibrary("kernel32", MyKernel32.class);
	private Pointer openedProcess = null;
	
	/**
	* Constructor of a ProcessAccess
	* @param processId ID of the process to open
	*/
	public ProcessAccess(int processId) throws Win32Exception
	{
		initProcessAccess(processId);
	}
	
	/**
	* Constructor of a ProcessAccess
	* this will open the first process found for the given name
	* @param processName Name of the process to search
	*/
	public ProcessAccess(String processName) throws Win32Exception
	{
		int processId = 0;
		long processId_l = ProcessAccess.FindProcessId(processName);
		processId = (int)processId_l;
		initProcessAccess(processId);
	}
	
	private void initProcessAccess(int processId) throws Win32Exception
	{
		int rights = PROCESS_VM_READ + PROCESS_VM_WRITE + VM_OPERATION;
		openedProcess = myKernel32.OpenProcess(rights, true, processId);
		if(openedProcess == null)
		{
			throw new Win32Exception(myKernel32.GetLastError());
		}
	}
	
	/**
	* Reads a Char at the given address
	* @param readAddress Address to read from in hexa
	* @return The char readen at the given address
	* @throws Win32Exception If there was an error reading the given address
	*/
	public char readChar(int readAddress) throws Win32Exception
	{
		return (char)readValue(readAddress, char.class);
	}
	
	/**
	* Reads a Short at the given address
	* @param readAddress Address to read from in hexa
	* @return The short readen at the given address
	* @throws Win32Exception If there was an error reading the given address
	*/
	public short readShort(int readAddress)
	{
		return (short)readValue(readAddress, short.class);
	}
	
	/**
	* Reads an Integer at the given address
	* @param readAddress Address to read from in hexa
	* @return The integer readen at the given address
	* @throws Win32Exception If there was an error reading the given address
	*/
	public int readInt(int readAddress)
	{
		return (int)readValue(readAddress, int.class);
	}
	
	/**
	* Reads a Long at the given address
	* @param readAddress Address to read from in hexa
	* @return The Long readen at the given address
	* @throws Win32Exception If there was an error reading the given address
	*/
	public long readLong(int readAddress)
	{
		return (long)readValue(readAddress, long.class);
	}
	
	/**
	* Reads a Float at the given address
	* @param readAddress Address to read from in hexa
	* @return The Float readen at the given address
	* @throws Win32Exception If there was an error reading the given address
	*/
	public float readFloat(int readAddress)
	{
		return (float)readValue(readAddress, float.class);
	}
	
	/**
	* Reads a Double at the given address
	* @param readAddress Address to read from in hexa
	* @return The Double readen at the given address
	* @throws Win32Exception If there was an error reading the given address
	*/
	public double readDouble(int readAddress)
	{
		return (double)readValue(readAddress, double.class);
	}
	
	private Object readValue(int readAddress, Class returnType) throws Win32Exception
	{
		int readSize = 0;
		if(returnType == char.class)
		{
			readSize = 1;
			return readValue(readAddress, returnType, readSize).getChar(0L);
		}
		else if(returnType == short.class)
		{
			readSize = 2;
			return readValue(readAddress, returnType, readSize).getShort(0L);
		}
		else if(returnType == int.class)
		{
			readSize = 4;
			return readValue(readAddress, returnType, readSize).getInt(0L);
		}
		else if(returnType == long.class)
		{
			readSize = 4;
			return readValue(readAddress, returnType, readSize).getLong(0L);
		}
		else if(returnType == float.class)
		{
			readSize = 4;
			return readValue(readAddress, returnType, readSize).getFloat(0L);
		}
		else if(returnType == double.class)
		{
			readSize = 8;
			return readValue(readAddress, returnType, readSize).getDouble(0L);
		}
		else
		{
			throw new Win32Exception(1);
		}
	}
	
	private Memory readValue(int readAddress, Class returnType, int readSize) throws Win32Exception
	{
		Memory output = new Memory(readSize);
		if(!myKernel32.ReadProcessMemory(openedProcess, readAddress, output, readSize, new IntByReference(0)))
		{
			throw new Win32Exception(myKernel32.GetLastError());
		}
		return output;
	}
	
	/**
	* Writes a Char to the given address
	* @param writeAddress Address to write to in hexa
	* @param newValue New Char to write at the given address
	* @return The number of bytes written
	* @throws Win32Exception If there was an error writing the new value at the given address
	*/
	public int writeValue(int writeAddress, char newValue) throws Win32Exception
	{
		int writeSize = 1;
		IntByReference written = new IntByReference(0);
		Memory toWrite = new Memory(writeSize);
		toWrite.setChar(0, newValue);
		if(!myKernel32.WriteProcessMemory(openedProcess, writeAddress, toWrite, 1, written))
		{
			throw new Win32Exception(myKernel32.GetLastError());
		}
		return written.getValue();
	}
	
	/**
	* Writes a Short to the given address
	* @param writeAddress Address to write to in hexa
	* @param newValue New Short to write at the given address
	* @return The number of bytes written
	* @throws Win32Exception If there was an error writing the new value at the given address
	*/
	public int writeValue(int writeAddress, short newValue) throws Win32Exception
	{
		int writeSize = 2;
		IntByReference written = new IntByReference(0);
		Memory toWrite = new Memory(writeSize);
		toWrite.setShort(0, newValue);
		if(!myKernel32.WriteProcessMemory(openedProcess, writeAddress, toWrite, 1, written))
		{
			throw new Win32Exception(myKernel32.GetLastError());
		}
		return written.getValue();
	}
	
	/**
	* Writes a Double to the given address
	* @param writeAddress Address to write to in hexa
	* @param newValue New Double to write at the given address
	* @return The number of bytes written
	* @throws Win32Exception If there was an error writing the new value at the given address
	*/
	public int writeValue(int writeAddress, double newValue) throws Win32Exception
	{
		int writeSize = 4;
		IntByReference written = new IntByReference(0);
		Memory toWrite = new Memory(writeSize);
		toWrite.setDouble(0, newValue);
		if(!myKernel32.WriteProcessMemory(openedProcess, writeAddress, toWrite, 1, written))
		{
			throw new Win32Exception(myKernel32.GetLastError());
		}
		return written.getValue();
	}
	
	/**
	* Writes a Float to the given address
	* @param writeAddress Address to write to in hexa
	* @param newValue New Float to write at the given address
	* @return The number of bytes written
	* @throws Win32Exception If there was an error writing the new value at the given address
	*/
	public int writeValue(int writeAddress, float newValue) throws Win32Exception
	{
		int writeSize = 4;
		IntByReference written = new IntByReference(0);
		Memory toWrite = new Memory(writeSize);
		toWrite.setFloat(0, newValue);
		if(!myKernel32.WriteProcessMemory(openedProcess, writeAddress, toWrite, 1, written))
		{
			throw new Win32Exception(myKernel32.GetLastError());
		}
		return written.getValue();
	}
	
	/**
	* Writes an Integer to the given address
	* @param writeAddress Address to write to in hexa
	* @param newValue New Integer to write at the given address
	* @return The number of bytes written
	* @throws Win32Exception If there was an error writing the new value at the given address
	*/
	public int writeValue(int writeAddress, int newValue) throws Win32Exception
	{
		int writeSize = 4;
		IntByReference written = new IntByReference(0);
		Memory toWrite = new Memory(writeSize);
		toWrite.setInt(0, newValue);
		if(!myKernel32.WriteProcessMemory(openedProcess, writeAddress, toWrite, 1, written))
		{
			throw new Win32Exception(myKernel32.GetLastError());
		}
		return written.getValue();
	}
	
	/**
	* Writes a Long to the given address
	* @param writeAddress Address to write to in hexa
	* @param newValue New Long to write at the given address
	* @return The number of bytes written
	* @throws Win32Exception If there was an error writing the new value at the given address
	*/
	public int writeValue(int writeAddress, long newValue) throws Win32Exception
	{
		int writeSize = 8;
		IntByReference written = new IntByReference(0);
		Memory toWrite = new Memory(writeSize);
		toWrite.setLong(0, newValue);
		if(!myKernel32.WriteProcessMemory(openedProcess, writeAddress, toWrite, 1, written))
		{
			throw new Win32Exception(myKernel32.GetLastError());
		}
		return written.getValue();
	}
}