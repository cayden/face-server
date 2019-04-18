/**
 * 
 */
package com.cayden.face;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.arcsoft.face.EngineConfiguration;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FaceSimilar;
import com.arcsoft.face.FunctionConfiguration;
import com.arcsoft.face.enums.ImageFormat;

/**
 * @author caydencui
 *
 */
public class FaceServer {

	public ImageInfo getRGBData(File file) {
		if (file == null)
			return null;
		ImageInfo imageInfo;
		try {
			// 将图片文件加载到内存缓冲区
			BufferedImage image = ImageIO.read(file);
			imageInfo = bufferedImage2ImageInfo(image);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return imageInfo;
	}

	private ImageInfo bufferedImage2ImageInfo(BufferedImage image) {
		ImageInfo imageInfo = new ImageInfo();
		int width = image.getWidth();
		int height = image.getHeight();
		// 使图片居中
		width = width & (~3);
		height = height & (~3);
		imageInfo.setWidth(width);
		imageInfo.setHeight(height);
		// 根据原图片信息新建一个图片缓冲区
		BufferedImage resultImage = new BufferedImage(width, height, image.getType());
		// 得到原图的rgb像素矩阵
		int[] rgb = image.getRGB(0, 0, width, height, null, 0, width);
		// 将像素矩阵 绘制到新的图片缓冲区中
		resultImage.setRGB(0, 0, width, height, rgb, 0, width);
		// 进行数据格式化为可用数据
		BufferedImage dstImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		if (resultImage.getType() != BufferedImage.TYPE_3BYTE_BGR) {
			ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
			ColorConvertOp colorConvertOp = new ColorConvertOp(cs, dstImage.createGraphics().getRenderingHints());
			colorConvertOp.filter(resultImage, dstImage);
		} else {
			dstImage = resultImage;
		}
		// 获取rgb数据
		imageInfo.setRgbData(((DataBufferByte) (dstImage.getRaster().getDataBuffer())).getData());
		return imageInfo;
	}

	class ImageInfo {
		public byte[] rgbData;
		public int width;
		public int height;

		public byte[] getRgbData() {
			return rgbData;
		}

		public void setRgbData(byte[] rgbData) {
			this.rgbData = rgbData;
		}

		public int getWidth() {
			return width;
		}

		public void setWidth(int width) {
			this.width = width;
		}

		public int getHeight() {
			return height;
		}

		public void setHeight(int height) {
			this.height = height;
		}
	}

	private void testFace(String[] args) {
		if(null==args||args.length<=0){
			System.out.println("请输入参数!");
			return;
		}
		String appId = "HJ8WHNec6BtnU9Sxpt1TkFRddBkkFUUKsxNYwxWc6HnK";
		String sdkKey = "3FwLdXWKJM77the4qqZGbBChF5wnEiE47CpndmfHFY16";
		System.out.println("开始激活...");
		FaceEngine faceEngine = new FaceEngine();
		// 激活引擎
		faceEngine.active(appId, sdkKey);
		System.out.println("激活成功");
		EngineConfiguration engineConfiguration = EngineConfiguration.builder()
				.functionConfiguration(FunctionConfiguration.builder().supportAge(true).supportFace3dAngle(true)
						.supportFaceDetect(true).supportFaceRecognition(true).supportGender(true).supportLiveness(true)
						.build())
				.build();
		// 初始化引擎
		faceEngine.init(engineConfiguration);

		ImageInfo imageInfo = getRGBData(new File(args[0]));
		ImageInfo imageInfo2 = getRGBData(new File(args[1]));
		System.out.println("开始人脸检测");
		// 人脸检测
		List<FaceInfo> faceInfoList = new ArrayList<FaceInfo>();
		faceEngine.detectFaces(imageInfo.getRgbData(), imageInfo.getWidth(), imageInfo.getHeight(),
				ImageFormat.CP_PAF_BGR24, faceInfoList);
		long s=System.currentTimeMillis();
		
		// 提取人脸特征
		FaceFeature faceFeature = new FaceFeature();
		faceEngine.extractFaceFeature(imageInfo.getRgbData(), imageInfo.getWidth(), imageInfo.getHeight(),
				ImageFormat.CP_PAF_BGR24, faceInfoList.get(0), faceFeature);
		long feature0=System.currentTimeMillis();
		System.out.println("单个特征提取：" + (feature0-s)+"ms");
		FaceFeature faceFeature2 = new FaceFeature();
		faceEngine.extractFaceFeature(imageInfo2.getRgbData(), imageInfo2.getWidth(), imageInfo2.getHeight(),
				ImageFormat.CP_PAF_BGR24, faceInfoList.get(0), faceFeature2);
		long feature=System.currentTimeMillis();
		// 人脸对比
		FaceFeature targetFaceFeature = new FaceFeature();
		targetFaceFeature.setFeatureData(faceFeature.getFeatureData());

		FaceFeature sourceFaceFeature = new FaceFeature();
		sourceFaceFeature.setFeatureData(faceFeature2.getFeatureData());

		FaceSimilar faceSimilar = new FaceSimilar();
	
		int result = faceEngine.compareFaceFeature(targetFaceFeature, sourceFaceFeature, faceSimilar);
		long e=System.currentTimeMillis();

		System.out.println("照片比对：" + result+" ,score:"+faceSimilar.getScore());
		
		System.out.println("特征提取：" + (feature-s)+"ms ,人脸比对:"+(e-feature)+"ms");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FaceServer faceServer=new FaceServer();
		faceServer.testFace(args);
	}

}
