package com.wezom.kiviremoteserver.bus;


import com.wezom.kiviremoteserver.interfaces.InitialMessage;
import com.wezom.kiviremoteserver.net.server.model.PreviewCommonStructure;

import java.util.List;

public class SendInitialEvent {
   private List<PreviewCommonStructure> structures;
   private InitialMessage initialMessage;

    public SendInitialEvent(InitialMessage initialMessage) {
        this.initialMessage = initialMessage;
    }

    public SendInitialEvent(List<PreviewCommonStructure> structures) {
        this.structures = structures;
    }

    public List<PreviewCommonStructure> getStructures() {
        return structures;
    }

    public InitialMessage getInitialMessage() {
        return initialMessage;
    }
}
