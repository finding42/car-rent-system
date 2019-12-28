package org.car.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.car.util.imageUtil;

@Controller

public class FileUploadController {

    protected JSONObject saveFile (HttpServletRequest request, MultipartFile[] files, String savePath ){

        JSONObject imageObject = new JSONObject();

        String originalFileName = files[0].getOriginalFilename();
        String saveFileName = UUID.randomUUID() + "." + originalFileName.substring(originalFileName.lastIndexOf(".")+1);
        String realPath = request.getSession().getServletContext().getRealPath(savePath);


        File file = new File(realPath, saveFileName);
        try {
            FileUtils.writeByteArrayToFile(file, files[0].getBytes());

            imageObject.put("realPath", realPath +"/" + saveFileName);
            System.out.println(realPath +"/" + saveFileName);
            imageObject.put("url", savePath + "/" +saveFileName);
            imageObject.put("fileName", saveFileName);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return imageObject;

    }

    JSONObject saveAndCutImage(JSONObject imageObject, String imageCutParams, double rotate){

        JSONObject result = new JSONObject();

        JSONObject imgCutParams = JSON.parseObject(imageCutParams);
        System.out.println("img cut params:"+imageCutParams);
        System.out.println(imgCutParams.getInteger("x")+"\t"+
                imgCutParams.getInteger("y")+"\t"+
                imgCutParams.getInteger("w")+"\t"+
                imgCutParams.getInteger("h")+"\t"+
                imgCutParams.getInteger("scaleWidth")+"\t"+
                imgCutParams.getInteger("scaleHeight"));

        String imageRealPath = imageObject.getString("realPath");
        if( imageRealPath != null ) {
            //开始剪裁图片
            try {
                if (imageUtil.imgCut(imageRealPath, imageRealPath,
                        imgCutParams.getInteger("x"),
                        imgCutParams.getInteger("y"),
                        imgCutParams.getInteger("w"),
                        imgCutParams.getInteger("h"),
                        imgCutParams.getInteger("scaleWidth"),
                        imgCutParams.getInteger("scaleHeight"))) {

                    if( rotate > 0 ){
                        System.out.println("旋转图片:"+rotate);
                        Thumbnails.of(new File(imageRealPath))
                                .size(1000,1000)
                                .rotate(-rotate)
                                .toFile(new File(imageRealPath));
                    }

                    System.out.println("剪裁成功！");

                    result.put("resultCode", 200);
                    result.put("resultMessage", "上传成功");
                    result.put("resultModel", imageObject);

                }
            } catch (Exception e) {
                System.out.println("剪裁失败！");
                e.printStackTrace();

                result.put("resultCode", 500);
                result.put("resultMessage", "图片剪裁失败！");
//      result.put("resultModel", imageObject);

            }
        }else{
            result.put("resultCode", 500);
            result.put("resultMessage", "图片保存失败！");
        }

        return result;

    }
}
