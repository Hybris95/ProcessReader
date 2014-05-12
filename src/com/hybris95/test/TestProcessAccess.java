package com.hybris95.test;

import com.hybris95.ProcessAccess;
import com.sun.jna.platform.win32.Win32Exception;

public class TestProcessAccess
{
	public static void main(String[] args)
	{
		String processName = "notepad.exe";
		int valueAddress = 0xFF;
		try
		{
			ProcessAccess myProcessAccess = new ProcessAccess(processName);
			char read_c = myProcessAccess.readChar(valueAddress);
			System.out.println("char read on " + processName + " at " + valueAddress + ": " + read_c);
		}
		catch(Win32Exception e)
		{
			System.err.println("Error while reading character @" + valueAddress + " for " + processName);
			System.err.println(e.getMessage() + ":" + e.getHR());
			e.printStackTrace(System.err);
		}
	}
}