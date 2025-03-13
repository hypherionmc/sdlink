package com.hypherionmc.sdlink.util.configeditor;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(staticName = "of")
@Getter
public final class SocketResponse {

    private String socketCode;
    private String message;

}
