package com.metal.fetcher.common;

/**
 * @Description 枚举类
 * Created by phil on 2016/6/30.
 */
public class CodeEnum {

    /**
     * 弹幕任务状态
     */
    public enum BarrageStatusEnum{
        INITIAL(0,"初始化"),
        RUNNING(1,"运行中"),
        FINISH(2,"任务完成"),
        END_SELF(-1,"手动结束"),
        END_EXCEPTION(-2,"异常结束");
        private int code;
        private String message;
        private BarrageStatusEnum(int code,String message){
            this.code = code;
            this.message = message;
        }
        public int getCode(){
            return code;
        }
        public String getMessage(){
            return message;
        }
    }

    /**
     * 平台
     */
    public enum PlatformEnum{
        TENG_XUN(0,"腾讯"),
        YUO_KU(1,"优酷"),
        I_QIYI(2,"爱奇艺"),
        LE_TV(3,"乐视"),
        SO_HU(4,"搜狐"),
        BILI_BILI(5,"bilibili");

        private int code;
        private String des;
        private PlatformEnum(int code,String des){
            this.code = code;
            this.des = des;
        }
        public int getCode(){
            return code;
        }
        public String getDes(){
            return des;
        }
    }
}
