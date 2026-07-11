package com.chaobo.scm.supplier.domain.item;

public enum SupplyStatus {
    AVAILABLE(1,"可供"), PAUSED(2,"暂停"), DISCONTINUED(3,"停供");
    private final int code; private final String label;
    SupplyStatus(int code,String label){this.code=code;this.label=label;}
    public int code(){return code;} public String label(){return label;}
    public static SupplyStatus fromCode(int code){for(var v:values())if(v.code==code)return v;throw new IllegalArgumentException("未知供货状态: "+code);}
}
