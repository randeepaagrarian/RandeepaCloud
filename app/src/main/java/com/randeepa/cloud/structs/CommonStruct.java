package com.randeepa.cloud.structs;

public class CommonStruct {
    private String id;
    private String name;

    public CommonStruct(String id, String name) {
        this.id = id;
        this.name = name;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof CommonStruct){
            CommonStruct showroomDealer = (CommonStruct)obj;
            if(showroomDealer.getName().equals(name) && showroomDealer.getId()==id ) return true;
        }

        return false;
    }
}
