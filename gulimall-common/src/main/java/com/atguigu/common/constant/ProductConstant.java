package com.atguigu.common.constant;

import lombok.Data;

/**
 * @author tangyao
 * @version 1.0.0
 * @Description TODO
 * @createTime 2020年09月03日 08:39:00
 */
public class ProductConstant {
    public enum AttrEnum {
        //属性类型[0-销售属性，1-基本属性]
        ATTR_TYPE_SALE(0, "销售属性"),
        ATTR_TYPE_BASE(1, "基本属性");

        private int code;
        private String value;

        AttrEnum(int code, String value) {
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
    public enum StatusEnum {
        //上架状态[0 -新建 2 - 下架，1 - 上架]
        NEW_SPU(0, "新建"),
        SPU_UP(1, "上架"),
        SPU_DOWN(2, "下架");

        private int code;
        private String value;

        StatusEnum(int code, String value) {
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
