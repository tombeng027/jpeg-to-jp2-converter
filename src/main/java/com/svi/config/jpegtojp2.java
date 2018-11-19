package com.svi.config;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

import com.github.jaiimageio.jpeg2000.J2KImageWriteParam;
import com.github.jaiimageio.jpeg2000.impl.J2KImageWriter;
import com.github.jaiimageio.jpeg2000.impl.J2KImageWriterSpi;

public class jpegtojp2 {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		initializeConfig();
		File input = new File(AppConfig.INPUT_PATH.value());
		String output = AppConfig.OUTPUT_PATH.value();
		
	        Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix("jp2");
	        if(writers.hasNext());
	        DecimalFormat d = new DecimalFormat("##.00");
			Double in = Double.parseDouble(AppConfig.COMPRESSION_RATIO.value());
			double start = System.currentTimeMillis();
			double end;
//			System.out.println("Writing image with encoding rate of " + writeParams.getEncodingRate() + "\n");
			int CPUCores =  Runtime.getRuntime().availableProcessors();
			ExecutorService pool = Executors.newFixedThreadPool(CPUCores);
			ThreadPoolExecutor executor = (ThreadPoolExecutor) pool;
			
			for(File entry : input.listFiles()){
				entry.getName();
				pool.submit(()->{
					try{
						convertJPG(entry, writers, output, d, in);
						System.out.println("Total Threads: " + executor.getPoolSize() + "\n" + 
						"Active Threads: " + executor.getActiveCount() + "\n" + 
						"Total Tasks: " + executor.getTaskCount() + "\n" +
						"Completed Tasks: " + (executor.getCompletedTaskCount() + 1));
						
					}catch (Exception e){
						e.printStackTrace();
					}
				});
				
				 
			}
			pool.shutdown();
			try {
				pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally{
				end = System.currentTimeMillis();
				System.out.println("\nWriting took " + d.format((end - start)/1000) + "secs \n"
						+ "Completed Tasks: " + executor.getCompletedTaskCount());
				System.out.println("Done Converting.");
				
			}
			 
		
	}
	
	private static void convertJPG(File entry , Iterator<ImageWriter> writers, String output, DecimalFormat d,
			Double in){
		File f = null;
		try{
			J2KImageWriterSpi spi = new J2KImageWriterSpi();
			J2KImageWriter writer = new J2KImageWriter(spi);
	        J2KImageWriteParam writeParams = (J2KImageWriteParam) writer.getDefaultWriteParam();
			writeParams.setLossless(false);
			double compressionRatio = Double.parseDouble(d.format(101/in));
			writeParams.setEncodingRate(compressionRatio);
			String fileName = entry.getName().substring(0,entry.getName().length() - 4) + "_" + entry.getName().substring(entry.getName().length() - 3);
	       	BufferedImage image = ImageIO.read(entry);
	        f = new File(output, fileName  + ".jp2");
	//        double inSize = entry.length();
	        ImageOutputStream ios = ImageIO.createImageOutputStream(f);
	        System.out.println("Writing Image..." + entry.getName() + " Thread: " + Thread.currentThread().getName());
	        System.out.println(f.getCanonicalPath() + "\n");
			writer.setOutput(ios);
			writer.write(null, new IIOImage(image, null, null), writeParams);
			writer.dispose();
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			System.out.println(entry.getName() + " Thread: " + Thread.currentThread().getName() + " Image Saved!");
			System.out.println(entry.getName() + " Thread: " + Thread.currentThread().getName() + " Image size is : " + f.length() + " bytes");
		}
	}
	
	
	private static void initializeConfig() {
		try {
			AppConfig.setContext(new FileInputStream(new File(
					"config/config.properties")));
		} catch (FileNotFoundException e) {
			System.out.println("ConfigFile Not Found");
			e.printStackTrace();
			System.exit(0);
		}

	}

}
