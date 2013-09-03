package com.bigrequest.BigRequest;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.DefaultChannelPipeline;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.HttpVersion;

public class HttpClient {

	final static Logger LOG = Logger.getLogger(HttpClient.class);
	final static Executor bossExecutor = Executors.newCachedThreadPool();
	final static Executor workerExecutor = Executors.newCachedThreadPool();
	ClientBootstrap bootstrap;
	static String host;
	int port;
	public static Channel channel;

	Configuration conf;

	public HttpClient(Configuration conf) {
		this.conf = conf;
		this.host = conf.getString("host", "localhost");
		this.port = conf.getInt("port", 80);
		initClient(conf.getString("url", ""));
	}

	public void initClient(final String url) {
		ChannelFactory factory = new NioClientSocketChannelFactory(
				bossExecutor, workerExecutor);
		bootstrap = new ClientBootstrap(factory);
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() {
				DefaultChannelPipeline pipeline = new DefaultChannelPipeline();
//				pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192,
//						Delimiters.lineDelimiter()));
				pipeline.addLast("decoder", new HttpResponseDecoder());
				pipeline.addLast("encoder", new HttpRequestEncoder());
				pipeline.addLast("handler", new HttpCleintHandler(url));
				return pipeline;
			}
		});

		ChannelFuture future = bootstrap.connect(new InetSocketAddress(host,
				port));
		future.addListener(new ChannelFutureListener() {
			
			public void operationComplete(ChannelFuture future) throws Exception {
				 if (!future.isSuccess()) {                                                  
			            future.getCause().printStackTrace();                                 
			            return;                                                                 
			        }                                                                           
			}
		});
		channel = future.getChannel();

		/*if (!future.isSuccess()) {
			future.getCause().printStackTrace();
			bootstrap.releaseExternalResources();
			return;
		}*/

	}

	public static class HttpCleintHandler extends SimpleChannelHandler {

		String url;
		
		public HttpCleintHandler(String url){
			this.url = url;
		}
		@Override
		public void channelBound(ChannelHandlerContext ctx, ChannelStateEvent e) {
		    System.out.println("Bound: " + e.getChannel().isBound());
		}

		@Override
		public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		    System.out.println("Connected: " + e.getChannel().isConnected());
		    System.out.println("Connected: " + e.getChannel().getRemoteAddress());
		    ctx.getChannel().write(this.createRequest());
		}

		@Override
		public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) {
		    System.out.println("Closed: " + e.getChannel());
		}

		@Override
		public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		    System.out.println("Disconnected: " + e.getChannel());
		}

		@Override
		public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
		    System.out.println("Open: " + e.getChannel().isOpen());
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		    System.out.println("Error: " + e.getCause());
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		    System.out.println("Message: " + e.getMessage());
		}
		
		private HttpRequest createRequest() {
	        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, this.url);
				request.setHeader(HttpHeaders.Names.HOST, host);
	        request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
	        request.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);

	        return request;
	    }
	}
	
	public void makeRquest(String url){
		HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, url);
		request.setHeader("Connection", true);
		request.setHeader("Accept", "text/html");
		 request.setHeader(HttpHeaders.Names.HOST, host);                            
	        request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
		ChannelFuture future = channel.write(request);
		System.out.println(future.isSuccess());
		future.addListener(ChannelFutureListener.CLOSE);
	}
	
}

class ConnectOk1 implements ChannelFutureListener {
	ConnectOk1(){
}
	
	public void operationComplete(ChannelFuture future){
        if (!future.isSuccess()) {                                                  
            future.getCause().printStackTrace();                                 
            return;                                                                 
        }                                                                           
        Channel channel = future.getChannel();                                                                            
    }
}     
