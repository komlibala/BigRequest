package com.bigrequest.BigRequest;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
/**                                                                                 
 * A  asynchronous HTTP client implements with Netty  
 *                                   
 *                                                                                  
 * @author fengshihao (fengshihao@sohu-inc.com)                          
 *                                                                                  
 * @version 0.1     
 */                 

public class NettyHttpClient{
                           
    private ClientBootstrap bootstrap = null;
    
    ChannelGroup allChannels = null;
    NettyHttpClient(){ 
    	bootstrap = new ClientBootstrap(                            
        new NioClientSocketChannelFactory(                                  
                Executors.newCachedThreadPool(),                            
                Executors.newCachedThreadPool()));
        bootstrap.setPipelineFactory(new HttpClientPipelineFactory()); 
        allChannels = new DefaultChannelGroup();
        }
 
 
    public void setOption(String key, Object value) {
    	bootstrap.setOption(key, value);
    }
    
    public ChannelPipeline get(String url) throws Exception{
    	return retrieve("GET", url);
    }
     
    public ChannelPipeline delete(String url) throws Exception{
    return retrieve("DELETE", url);
    }
     
    public ChannelPipeline post(String url, Map<String, Object> data) throws Exception{
    	return retrieve("POST", url, data);
    }
    
    public ChannelPipeline retrieve(String method, String url) throws Exception{
    return retrieve(method, url, null, null);
    }
    
    public ChannelPipeline retrieve(String method, String url, Map<String, Object> data ) throws Exception{
	   return retrieve(method, url, data, null);
    }
    
    public ChannelPipeline retrieve(String method, String url, Map<String, Object>data, Map<String, String> cookie) throws Exception{
    	if(url == null) throw new Exception("url is null") ;
    	URI uri = new URI(url);                                                 
        String scheme = uri.getScheme() == null? "http" : uri.getScheme();          
        String host = uri.getHost() == null? "localhost" : uri.getHost();
        int port = uri.getPort() == -1? 80 : uri.getPort();
                                                                                    
        if (!scheme.equals("http")) {                                               
            throw new Exception("just support http protocol") ;                        
        }
        
        HttpRequest request = new DefaultHttpRequest(                               
                HttpVersion.HTTP_1_1, HttpMethod.valueOf(method), uri.getRawPath());
                        
        request.setHeader(HttpHeaders.Names.HOST, host);                            
        request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        if(cookie != null){
        CookieEncoder httpCookieEncoder = new CookieEncoder(false);
        for (Map.Entry<String, String> m : cookie.entrySet()) {
			httpCookieEncoder.addCookie(m.getKey(), m.getValue());
			request.setHeader(HttpHeaders.Names.COOKIE, httpCookieEncoder.encode());
			}
        }
        return retrieve(request,port);
         
    }
    
    public ChannelPipeline retrieve(HttpRequest request,int port)throws Exception{
//    	URI uri = new URI(request.getUri());          
//        int port = uri.getPort() == -1? 80 : uri.getPort();
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(request.getHeader(HttpHeaders.Names.HOST) , port));
        future.addListener(new ConnectOk(request));
        allChannels.add(future.getChannel());
        return   future.getChannel().getPipeline();
    } 
   
    public void close(){
    	allChannels.close().awaitUninterruptibly();
        bootstrap.releaseExternalResources();
    }
    
    
    public static void main(String[] args) throws InterruptedException{
    	String url = "http://192.168.1.91:7101/apphandler/request/0.2.0/android/14131C047A504046465945514455435D888427CC/200.95.162.199/Mozilla%2F5.0%20(Linux%3B%20U%3B%20Android%202.1-update1%3B%20en-au%3B%20T3020%20Build%2FERE27)%20AppleWebKit%2F530.17%20(KHTML%2C%20like%20Gecko)%20Version%2F4.0%20Mobile%20Safari%2F530.17/live/text/text+picture/1/imei/8762345898437598/A/NA/iueuytgw";
        NettyHttpClient hc = new NettyHttpClient();
        int threadsCount = 100;
        int reqPerThread = 1000;
        ExecutorService thread = Executors.newCachedThreadPool();
        List<Future<Map<Long, List<Long>>>> futures = new ArrayList<Future<Map<Long,List<Long>>>>();
        
        for(int i=0;i<threadsCount;i++){
        	Callable<Map<Long,List<Long>>> makeRequest = new MakeRequest(reqPerThread, hc,url);
        	Future<Map<Long, List<Long>>> submit = thread.submit(makeRequest);
        	futures.add(submit);
        }
        thread.shutdown();
        thread.awaitTermination(10000,TimeUnit.SECONDS);
        
        hc.close();
        for(Future<Map<Long, List<Long>>> future : futures){
        	try {
        		Map<Long,List<Long>> res = future.get();
        		for (Map.Entry<Long,List<Long>> entry : res.entrySet())
        		{
        			List<Long> value = entry.getValue(); 
        			long maxTime = Collections.max(value);
        			long minTime = Collections.min(value);
        			long avg = average(value);
        		    System.out.println("Thread "+entry.getKey() + ": Minimum Time taken " + minTime + " and Max Time taken : "+maxTime+" and Average Time taken : "+avg);
        		}
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    	 }
    
    public static long average(List<Long> list) {
        if (list == null || list.isEmpty())
            return 0;
        long sum = 0;
        int n = list.size();
        for (int i = 0; i < n; i++)
            sum += list.get(i);
        return (sum) / n;
    }
}        

class MakeRequest implements Callable<Map<Long,List<Long>>>{
	
	int reqPerThread;
	NettyHttpClient hc;
	String url;
	Map<Long,List<Long>> reqTime = new HashMap<Long,List<Long>>();
	
	public MakeRequest(int reqPerThread,NettyHttpClient hc,String url){
		this.reqPerThread = reqPerThread;
		this.hc = hc;
		this.url = url;
	}

	public Map<Long, List<Long>> call() throws Exception {
		 List<Long> reqTimeList = null;
		    for (int i = 1; i <= reqPerThread; i++) {
		    	long start = System.currentTimeMillis();
		    	 try {
		         	ChannelPipeline line = hc.get(url);
		         line.addLast("handler", new HttpResponseHandler());
		         Thread.sleep(20);
		         long key = Thread.currentThread().getId();
		         if(reqTime.containsKey(key)){
		        	 reqTimeList.add((System.currentTimeMillis() - (start + 20)));
		        	 reqTime.put(key, reqTimeList);
		         }else{
		        	 reqTimeList = new ArrayList<Long>();
		        	 reqTimeList.add((System.currentTimeMillis() - (start + 20)));
		        	 reqTime.put(key, reqTimeList);
		         }
		         } catch(Exception ex) {
		         	ex.printStackTrace();
		         }
		    }
		    return reqTime;
	}
}
                                        

class ConnectOk implements ChannelFutureListener {
	private HttpRequest request=null;
	ConnectOk(HttpRequest req){
	this.request = req;
}
	
	public void operationComplete(ChannelFuture future){
        if (!future.isSuccess()) {                                                  
            future.getCause().printStackTrace();                                 
            return;                                                                 
        }                                                                           
        Channel channel = future.getChannel();                                                                            
        channel.write(request);                                                  
    }
}                                        