package com.esri.ges.monitor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.http.HttpHost;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;

public class NaiveSocketFactory implements LayeredConnectionSocketFactory {

	@Override
	public Socket createSocket(HttpContext context) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Socket connectSocket(int connectTimeout, Socket sock, HttpHost host,
			InetSocketAddress remoteAddress, InetSocketAddress localAddress,
			HttpContext context) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Socket createLayeredSocket(Socket socket, String target, int port,
			HttpContext context) throws IOException, UnknownHostException {
		// TODO Auto-generated method stub
		return null;
	}

}
