package com.syj.tcpentrypoint.protocol;
/**
 * 
*  @des    :This class only describe the re protocol
 * @author:shenyanjun1
 * @date   :2018-12-14 17:43
 */

/**
 * Title: 自定义REProtocol<br>
 * <p/>
 * Description: 自主研发的RE(rulesEngine)协议 结构如下:
 *
 * RE Protocol 
 * MEGICCODE(1B) Protocol Identify FF 
 * =================Protocol* Header Begin======================== 
 * FULLLENGTH(4B): length of (header+body),not include MEGICCODE 
 * HEADERLENGTH(2B): length of (PROTOCOLTYPE+...+header  tail), not include FULLLENGTH and HEADERLENGTH 
 * MESSAGETYPE(1B) request/response/heartbeat/callback? 
 * MSGID(4B): message id 
 * [OPT]ATTRMAP :
*	MAP_SIZE(1B) size of attr map 
*		 { 
*			ATTR_KEY(1B) key of attr 
* 			ATTR_TYPE(1B) 1:int;2:string; 3:byte; 4:short 
* 			ATTR_VAL(?B) int:(4B); string:length(2B)+data;byte:(1B); short:(2B) } 
*        }
 ===============Protocol Header End============================= 
 ===============Protocol Body  Begin===========================
 Byte[] 
 * ===============Protocol Body End=============================
 */
public class REProtocol {
	
}
