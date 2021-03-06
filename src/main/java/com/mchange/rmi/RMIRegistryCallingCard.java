/*
 * Distributed as part of mchange-commons-java 0.2.11
 *
 * Copyright (C) 2015 Machinery For Change, Inc.
 *
 * Author: Steve Waldman <swaldman@mchange.com>
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of EITHER:
 *
 *     1) The GNU Lesser General Public License (LGPL), version 2.1, as 
 *        published by the Free Software Foundation
 *
 * OR
 *
 *     2) The Eclipse Public License (EPL), version 1.0
 *
 * You may choose which license to accept if you wish to redistribute
 * or modify this work. You may offer derivatives of this work
 * under the license you have chosen, or you may provide the same
 * choice of license which you have been offered here.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received copies of both LGPL v2.1 and EPL v1.0
 * along with this software; see the files LICENSE-EPL and LICENSE-LGPL.
 * If not, the text of these licenses are currently available at
 *
 * LGPL v2.1: http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 *  EPL v1.0: http://www.eclipse.org/org/documents/epl-v10.php 
 * 
 */

package com.mchange.rmi;

import java.io.*;
import java.net.*;
import java.rmi.*;
import java.rmi.registry.*;
import com.mchange.io.*;

public class RMIRegistryCallingCard implements CallingCard, Serializable
{
  transient Remote cached = null; //really transient

  transient /* not */ String url;

  public RMIRegistryCallingCard(String regHost, int reg_port, String name)
    {this.url = "//" + regHost.toLowerCase() + ':' + reg_port + '/' + name;}

  public RMIRegistryCallingCard(String regHost, String name)
    {this(regHost, Registry.REGISTRY_PORT, name);}

  public boolean equals(Object o)
    {return (o instanceof RMIRegistryCallingCard) && this.url.equals(((RMIRegistryCallingCard) o).url);}

  public int hashCode()
    {return url.hashCode();}

  public Remote findRemote() throws ServiceUnavailableException, RemoteException
    {
      if (cached instanceof Checkable)
	{
	  try
	    {
	      ((Checkable) cached).check();
	      return cached;
	    }
	  catch (RemoteException e)
	    {
	      cached = null;
	      return findRemote();
	    }
	}
      else
	{
	  try
	    {
	      Remote r = Naming.lookup(url);
	      if (r instanceof Checkable)
		cached = r;
	      return r;
	    }
	  catch (NotBoundException e)
	    {throw new ServiceUnavailableException("Object Not Bound: " + url);}
	  catch (MalformedURLException e) //I'd like to check for this in constructor, but how?
	    {throw new ServiceUnavailableException("Uh oh. Bad url. It never will be available: " + url);}
	}
    }

  public String toString()
    {return super.toString() + " [" + url + "];";}

  //Serialization stuff
  static final long serialVersionUID = 1;
  private final static short VERSION = 0x0001;
  
  private void writeObject(ObjectOutputStream out) throws IOException
    {
      out.writeShort(VERSION);
      
      out.writeUTF(url);
    }
  
  private void readObject(ObjectInputStream in) throws IOException
    {
      short version = in.readShort();
      switch (version)
	{
	case 0x0001:
	  url = in.readUTF();
	  break;
	default:
	  throw new UnsupportedVersionException(this.getClass().getName() + "; Bad version: " + version);
	}
    }
}
  
