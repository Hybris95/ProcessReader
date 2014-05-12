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
	
	interface MyKernel32 extends StdCallLibrary
	{
		boolean WriteProcessMemory(Pointer p, int address, Pointer buffer, int size, IntByReference written);
		boolean ReadProcessMemory(Pointer hProcess, int inBaseAddress, Pointer outputBuffer, int nSize, IntByReference outNumberOfBytesRead);
		Pointer OpenProcess(int desired, boolean inherit, int pid);
		int GetLastError();
	}
	
	private MyKernel32 myKernel32 = (MyKernel32)Native.loadLibrary("kernel32", MyKernel32.class);
	private Pointer openedProcess = null;
	
	public ProcessAccess(int processId) throws Win32Exception
	{
		int rights = PROCESS_VM_READ + PROCESS_VM_WRITE + VM_OPERATION;
		openedProcess = myKernel32.OpenProcess(rights, true, processId);
		if(openedProcess == null)
		{
			throw new Win32Exception(myKernel32.GetLastError());
		}
	}
	
	public Object readValue(int readAddress, Class returnType) throws Win32Exception
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