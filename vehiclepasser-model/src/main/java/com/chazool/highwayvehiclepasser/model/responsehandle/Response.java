package com.chazool.highwayvehiclepasser.model.responsehandle;

import lombok.Data;

@Data
public class Response {


        private Status status;
        private Object data;

        private enum Status{
            SUCCESS, FAIL
        }

        private Response(Status status,Object data){
            this.status=status;
            this.data=data;
        }

        public static Response success(Object data){
            return new Response(Status.SUCCESS,data);
        }

        public static Response fail(Object data){
            return new Response(Status.FAIL,data);
        }




}
