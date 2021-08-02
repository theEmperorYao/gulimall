package com.atguigu.common.constant;

/**
 * @author tangyao
 * @version 1.0.0
 * @Description TODO
 * @createTime 2020年09月25日 21:36:00
 */
public class WareConstant {
    public enum PurchurseStatusEnum {
        //
        CREATED(0, "新建"),
        ASSIGNED(1, "已分配"),
        RECEIVED(2, "已领取"),
        FINISHED(3, "已完成"),
        HASERROR(4, "有异常");

        private int code;
        private String value;

        PurchurseStatusEnum(int code, String value) {
            this.code = code;
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
    public enum PurchurseDetailStatusEnum {
        //
        CREATED(0, "新建"),
        ASSIGNED(1, "已分配"),
        BUYING(2, "正在采购"),
        FINISHED(3, "已完成"),
        HASERROR(4, "采购失败");

        private int code;
        private String value;

        PurchurseDetailStatusEnum(int code, String value) {
            this.code = code;
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
