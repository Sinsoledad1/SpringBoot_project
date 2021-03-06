package org.echoiot.controller;
import org.echoiot.entity.bean.CommonResult;
import org.echoiot.entity.bean.Image;
import org.echoiot.entity.pojo.User;
import org.echoiot.util.*;
import org.echoiot.entity.bean.VerifyCode;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @author BeiChen
 * @version 1.0
 * @date 2020/11/30 10:27
 */
@RestController
@RequestMapping("/utils")
public class UtilsController {
    private final ImageUploadUtil imageUploadUtil;
    private final IpUtil ipUtil;


    public UtilsController(ImageUploadUtil imageUploadUtil, IpUtil ipUtil, RedisTemplate redisTemplate) {
        this.imageUploadUtil = imageUploadUtil;
        this.ipUtil = ipUtil;
    }

    /**
     * 上传图片接口
     * @param req
     * @param image
     * @return
     */
    @PostMapping("/image")
    public CommonResult<Object> upload(HttpServletRequest req, @RequestParam("image") MultipartFile image) {
        // FIXME: 2020/10/30 上线之前务必修改此处！
        User user = (User) req.getAttribute("User");
        if (user == null){
            return new CommonResult<>(401,"用户未登录",null);
        }
        Image finalImage = imageUploadUtil.uploadImage(image);
        return new CommonResult<>(200, "图片上传成功", finalImage);
    }

    /**
     *
     * 生成验证码并存入session
     * @param request
     * @param response
     * @throws IOException
     */
    @GetMapping("/verifyCode")
    public void getVerifyCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ipUtil.recording(request,"验证码","/verifyCode");
        VerifyCodeUtil verifyCodeUtil = new VerifyCodeUtil();
        //设置长宽
        VerifyCode verifyCode = verifyCodeUtil.generate(80, 28);
        String code = verifyCode.getCode();
        //将VerifyCode写入session
        HttpSession session = request.getSession();
        session.setAttribute("verifyCode", code);
        //设置响应头
        response.setHeader("Pragma", "no-cache");
        //设置响应头
        response.setHeader("Cache-Control", "no-cache");
        //在代理服务器端防止缓冲
        response.setDateHeader("Expires", 0);
        //设置响应内容类型
        response.setContentType("image/jpeg");
        response.getOutputStream().write(verifyCode.getImgBytes());
        response.getOutputStream().flush();

        System.out.println(code);
    }


}
