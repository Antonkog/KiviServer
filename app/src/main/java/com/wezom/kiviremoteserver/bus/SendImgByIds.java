package com.wezom.kiviremoteserver.bus;


import java.util.List;

public class SendImgByIds {
    List<String> ids;

    public SendImgByIds(List<String> ids) {
        this.ids = ids;
    }

    public List<String> getIds() {
        return ids;
    }

}
