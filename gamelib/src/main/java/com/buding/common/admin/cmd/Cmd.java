package com.buding.common.admin.cmd;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public interface Cmd {
	CmdResultModel execute(InputStream input, OutputStream output, Socket socket) throws Exception;
}
