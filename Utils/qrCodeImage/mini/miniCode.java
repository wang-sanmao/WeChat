public class TokenTestService {
	private String getQrCodeUtil(String url, Map<String,Object> param, Map<String,Object> line_color, HttpMethod requestMethod) throws ServiceException {
        RestTemplate rest = new RestTemplate();
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            param.put("line_color", line_color);
            logger.info("调用生成微信URL接口传参:" + param);
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            HttpEntity requestEntity = new HttpEntity(param, headers);
            ResponseEntity<byte[]> entity = rest.exchange(url,requestMethod, requestEntity, byte[].class, new Object[0]);
            logger.info("调用小程序生成微信永久小程序码URL接口返回结果:" + entity.getBody());
            byte[] result = entity.getBody();
            logger.info(Base64.encodeBase64String(result));
            inputStream = new ByteArrayInputStream(result);
            String returnUrl = uploadConfig.getQrCodeImage()+ UUIDGenerateUtil.uuid() +".png";
            File file = new File(returnUrl);
            if (!file.exists()){
                file.createNewFile();
            }
            outputStream = new FileOutputStream(file);
            int len = 0;
            byte[] buf = new byte[1024];
            while ((len = inputStream.read(buf, 0, 1024)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.flush();
            return returnUrl;
        } catch (Exception e) {
            logger.error("调用小程序生成微信永久小程序码URL接口异常",e);
        } finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(outputStream != null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    public String getMiniCode(WechatQrCodeRequest wechatQrCodeRequest) throws ServiceException {
        String userUuid = "";
        String accessToken = getAccessToken();
        if (!redisClient.lockRepeat(wechatQrCodeRequest)) {
            throw new ServiceException(ExceptionEnum.ORDER_LOCK_ERROR);
        }
        try {
            if (!StringUtils.isEmpty(wechatQrCodeRequest.getSessionId())) {
                String loginSessionStr = UserSessionUtil.getLoginSessionStr(redisClient, wechatQrCodeRequest.getSessionId());
                if (!StringUtils.isEmpty(loginSessionStr)) {
                    LoginSession loginSession = JSON.parseObject(loginSessionStr, LoginSession.class);
                    if (!StringUtils.isEmpty(loginSession.getUserUuid())) {
                        userUuid = loginSession.getUserUuid();
                    }
                }
            }
            String path = wechatQrCodeRequest.getPath();
            if (!StringUtils.isEmpty(userUuid)){
                path = path + "?userUuid="+userUuid;
            }
            Map<String,Object> param = new HashMap<>();
            param.put("path", path);
            param.put("width", wechatQrCodeRequest.getWidth());
            param.put("auto_color", false);
            Map<String,Object> line_color = new HashMap<>();
            line_color.put("r", 0);
            line_color.put("g", 0);
            line_color.put("b", 0);
            return getQrCodeUtil(ConfigApiUtil.WX_QR_CODE_URL.replace("ACCESS_TOKEN",accessToken),param,line_color,HttpMethod.POST);
        }finally {
            //解锁
            redisClient.unlockRepeat(wechatQrCodeRequest);
        }
    }
}
