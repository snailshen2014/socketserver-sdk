package com.syj.tcpentrypoint.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.spi.LoggerFactory;
import org.slf4j.Logger;

import com.syj.tcpentrypoint.error.RECodecException;
import com.syj.tcpentrypoint.msg.MessageHeader;
import com.syj.tcpentrypoint.protocol.REProtocol;

import io.netty.buffer.ByteBuf;

/**
 * Title:序列化注册工具类<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public final class CodecUtils {
	/**
	 * slf4j Logger for this class
	 */
	private final static Logger LOGGER = LoggerFactory.getLogger(CodecUtils.class);

	/**
	 * int 转 byte数组
	 *
	 * @param num int值
	 * @return byte[4]
	 */
	public static byte[] intToBytes(int num) {
		byte[] result = new byte[4];
		result[0] = (byte) (num >>> 24);// 取最高8位放到0下标
		result[1] = (byte) (num >>> 16);// 取次高8为放到1下标
		result[2] = (byte) (num >>> 8); // 取次低8位放到2下标
		result[3] = (byte) (num); // 取最低8位放到3下标
		return result;
	}

	/**
	 * byte数组转int
	 *
	 * @param ary byte[4]
	 * @return int值
	 */
	public static int bytesToInt(byte[] ary) {
		return (ary[3] & 0xFF) | ((ary[2] << 8) & 0xFF00) | ((ary[1] << 16) & 0xFF0000) | ((ary[0] << 24) & 0xFF000000);
	}

	/**
	 * short 转 byte数组
	 *
	 * @param num short值
	 * @return byte[2]
	 */
	public static byte[] short2bytes(short num) {
		byte[] result = new byte[2];
		result[0] = (byte) (num >>> 8); // 取次低8位放到2下标
		result[1] = (byte) (num); // 取最低8位放到3下标
		return result;
	}

	/**
	 * encode报文头
	 *
	 * @param header  MessageHeader
	 * @param byteBuf 报文
	 * @see REProtocol
	 */
	public static short encodeHeader(MessageHeader header, ByteBuf byteBuf) {
		System.out.println("buff capaticty=" + byteBuf.capacity());
		System.out.println("header=" + header);
		short headLength = Constants.REPROTOCOL_FRAME_NOMAP_SIZE; // 没有map 长度是5
		if (byteBuf.capacity() < Constants.REPROTOCOL_FRAME_NOMAP_SIZE)
			byteBuf.capacity(Constants.REPROTOCOL_FRAME_NOMAP_SIZE);
		int writeIndex = byteBuf.writerIndex();
		byteBuf.writeShort(headLength);
		byteBuf.writeByte(header.getMsgType());
		byteBuf.writeInt(header.getMsgId());
		
		if (header.getAttrMapSize() > 0) {
			headLength += map2bytes(header.getAttrMap(), byteBuf);
			byteBuf.setBytes(writeIndex, short2bytes(headLength)); // 替换head长度的两位
		}
		return headLength;
	}

	protected static short map2bytes(Map<Byte, Object> dataMap, ByteBuf byteBuf) {
		byteBuf.writeByte(dataMap.size());
		short s = 1;
		for (Map.Entry<Byte, Object> attr : dataMap.entrySet()) {
			byte key = attr.getKey();
			Object val = attr.getValue();
			if (val instanceof Integer) {
				byteBuf.writeByte(key);
				byteBuf.writeByte((byte) 1);
				byteBuf.writeInt((Integer) val);
				s += 6;
			} else if (val instanceof String) {
				byteBuf.writeByte(key);
				byteBuf.writeByte((byte) 2);
				byte[] bs = ((String) val).getBytes(Constants.DEFAULT_CHARSET);
				byteBuf.writeShort(bs.length);
				byteBuf.writeBytes(bs);
				s += (4 + bs.length);
			} else if (val instanceof Byte) {
				byteBuf.writeByte(key);
				byteBuf.writeByte((byte) 3);
				byteBuf.writeByte((Byte) val);
				s += 3;
			} else if (val instanceof Short) {
				byteBuf.writeByte(key);
				byteBuf.writeByte((byte) 4);
				byteBuf.writeShort((Short) val);
				s += 4;
			} else {
				throw new RECodecException("Value of attrs in message header must be byte/short/int/string");
			}
		}
		return s;
	}

	/**
	 * Decode报文头
	 *
	 * @param byteBuf      报文
	 * @param headerLength 报文长度
	 * @return MessageHeader
	 * @see JSFProtocol
	 */
	public static MessageHeader decodeHeader(ByteBuf byteBuf, int headerLength) {
		byte msgType = byteBuf.readByte();
		int messageId = byteBuf.readInt();
		MessageHeader header = new MessageHeader();
		header.setValues(msgType, messageId);
		if (headerLength > Constants.REPROTOCOL_FRAME_NOMAP_SIZE) {
			bytes2Map(header.getAttrMap(), byteBuf);
		}
		return header;
	}

	protected static void bytes2Map(Map<Byte, Object> dataMap, ByteBuf byteBuf) {
		byte size = byteBuf.readByte();
		for (int i = 0; i < size; i++) {
			byte key = byteBuf.readByte();
			byte type = byteBuf.readByte();
			if (type == 1) {
				int value = byteBuf.readInt();
				dataMap.put(key, value);
			} else if (type == 2) {
				int length = byteBuf.readShort();
				byte[] dataArr = new byte[length];
				byteBuf.readBytes(dataArr);
				dataMap.put(key, new String(dataArr, Constants.DEFAULT_CHARSET));
			} else if (type == 3) {
				byte value = byteBuf.readByte();
				dataMap.put(key, value);
			} else if (type == 4) {
				short value = byteBuf.readShort();
				dataMap.put(key, value);
			} else {
				throw new RECodecException("Value of attrs in message header must be byte/short/int/string");
			}
		}
	}

	public static Field[] getFields(Class<?> targetClass) {
		if (targetClass == null) {
			return new Field[0];
		}
		List<Field[]> succ = new ArrayList<Field[]>();
		for (Class<?> c = targetClass; c != Object.class && c != null; c = c.getSuperclass()) {
			Field[] fields = c.getDeclaredFields();
			succ.add(fields);
		}
		List<Field> resultList = new ArrayList<Field>();
		for (Field[] fields : succ) {
			for (Field f : fields) {
				int mod = f.getModifiers();
				// default mode:
				// transient, static, final : Ignore
				// 2015.01.29 支持final序列化
				if (Modifier.isStatic(mod) || Modifier.isTransient(mod)) {
					continue;
				}
				f.setAccessible(true);
				resultList.add(f);
			}
		}
		Field[] result = new Field[resultList.size()];
		for (int i = resultList.size() - 1; i >= 0; i--) {
			result[i] = resultList.get(i);
		}
		return result;
	}

	/**
	 * 检查是否支持多语言 检查规则： 1、方法参数不包含接口类型，必须全部为具体实现类 2、集合类全部指明泛型，且泛型全部是具体实现类
	 * 3、参数类中不允许出现Object、泛型等字段
	 * 
	 * @param serviceClass
	 * @return
	 */
	public static boolean isSupportCrossLang(Class<?> serviceClass) {
		if (serviceClass == null) {
			return false;
		}
		Method methodList[] = serviceClass.getDeclaredMethods();
		for (Method method : methodList) {
			Class<?> params[] = method.getParameterTypes();
			Type types[] = method.getGenericParameterTypes();
			for (int i = 0; i < params.length; i++) {
				Class<?> parameter = params[i];
				Type type = types[i];
				// 集合类
				boolean result = checkParameter(parameter, type, new HashSet<Class<?>>());
				if (!result) {
					return result;
				}
			}

			Class<?> returnClass = method.getReturnType();
			boolean result = checkParameter(returnClass, method.getGenericReturnType(), new HashSet<Class<?>>());
			if (!result) {
				return result;
			}
		}

		return true;
	}

	/**
	 * 检查具体参数类
	 * 
	 * @param parameter
	 * @param type
	 * @return
	 */
	private static boolean checkParameter(Class<?> parameter, Type type, Set<Class<?>> checkSet) {
		if (checkSet.contains(parameter) && (type == null || type instanceof Class<?>)) {
			return true;
		}
		// 基本类型
		if (parameter.isPrimitive() || parameter.isEnum()) {
			checkSet.add(parameter);
			return true;
		}
		// 抽象类不支持跨语言
		if (!parameter.isArray() && Modifier.isAbstract(parameter.getModifiers())) {
			return false;
		}
		// 接口或者Object、注解类不支持跨语言
		if (parameter.isInterface() || Object.class.equals(parameter) || parameter.isAnnotation()
				|| Annotation.class.isAssignableFrom(parameter)) {
			return false;
		}

		// 集合类
		if (Collection.class.isAssignableFrom(parameter) || Map.class.isAssignableFrom(parameter)) {
			if (type == null || type instanceof Class) {
				return false;
			}
			Type paramType[] = ((ParameterizedType) type).getActualTypeArguments();
			if (paramType.length == 0) {
				return false;
			}
			boolean result = true;
			for (Type pt : paramType) {
				if (pt instanceof Class) {
					result = checkParameter((Class<?>) pt, null, checkSet);
					if (!result) {
						break;
					}
				} else if (pt instanceof ParameterizedType) {
					ParameterizedType pType = (ParameterizedType) pt;
					Type rawType = pType.getRawType();
					if (rawType instanceof Class) {
						result = checkParameter((Class<?>) rawType, pt, checkSet);
						if (!result) {
							break;
						}
					} else {
						return false;
					}
				} else if (pt instanceof GenericArrayType) {
					GenericArrayType compsType = (GenericArrayType) pt;
					Type realType = compsType.getGenericComponentType();
					// 直到不是数组类型时
					if (realType instanceof GenericArrayType) {
						while (realType instanceof GenericArrayType) {
							realType = ((GenericArrayType) realType).getGenericComponentType();
						}
					}
					if (realType instanceof Class) {
						result = checkParameter((Class<?>) realType, realType, checkSet);
					} else if (realType instanceof ParameterizedType) {
						ParameterizedType realParamType = (ParameterizedType) realType;
						Type rawType = realParamType.getRawType();
						if (rawType instanceof Class) {
							result = checkParameter((Class<?>) rawType, realParamType, checkSet);
						} else {
							return false;
						}
					} else {
						return false;
					}
					if (!result) {
						break;
					}
				} else {
					return false;
				}
			}
			return result;
		}

		// 数组
		if (parameter.isArray()) {
			if (type instanceof GenericArrayType) {
				GenericArrayType compsType = (GenericArrayType) type;
				return checkParameter(parameter.getComponentType(), compsType.getGenericComponentType(), checkSet);
			} else if (type instanceof Class) {
				return checkParameter(parameter.getComponentType(), parameter.getComponentType(), checkSet);
			} else {
				return false;
			}
		}
		// Bean
		checkSet.add(parameter);
		boolean result = false;
		Field fieldList[] = getFields(parameter);
		for (Field field : fieldList) {
			int mod = field.getModifiers();
			if (Modifier.isStatic(mod) || Modifier.isFinal(mod) || Modifier.isTransient(mod)) {
				continue;
			}
			Class<?> fieldClass = field.getType();
			// 自包类忽略
			if (fieldClass.equals(parameter)) {
				continue;
			}
			Type fType = field.getGenericType();

			if (fType instanceof ParameterizedType) {
				// 接口或者Object不支持跨语言
				if (fieldClass.isInterface() || Object.class.equals(fieldClass)) {
					return false;
				}
				ParameterizedType pfType = (ParameterizedType) fType;
				Type actType[] = pfType.getActualTypeArguments();
				for (Type t : actType) {
					if (t instanceof Class) {
						Class<?> tClass = (Class<?>) t;
						if (tClass.equals(parameter)) {
							continue;
						}
						if (tClass.isEnum()) {
							continue;
						}
						result = checkParameter(tClass, null, checkSet);
						if (!result) {
							return result;
						}
					} else if (t instanceof ParameterizedType) {
						ParameterizedType realParamType = (ParameterizedType) t;
						Type rawType = realParamType.getRawType();
						if (rawType instanceof Class) {
							result = checkParameter((Class<?>) rawType, realParamType, checkSet);
							if (!result) {
								return result;
							}
						} else {
							return false;
						}
					} else if (t instanceof GenericArrayType) {
						GenericArrayType compsType = (GenericArrayType) t;
						Type realType = compsType.getGenericComponentType();
						// 直到不是数组类型时
						if (realType instanceof GenericArrayType) {
							while (realType instanceof GenericArrayType) {
								realType = ((GenericArrayType) realType).getGenericComponentType();
							}
						}
						if (realType instanceof Class) {
							result = checkParameter((Class<?>) realType, realType, checkSet);
						} else if (realType instanceof ParameterizedType) {
							ParameterizedType realParamType = (ParameterizedType) realType;
							Type rawType = realParamType.getRawType();
							if (rawType instanceof Class) {
								result = checkParameter((Class<?>) rawType, realParamType, checkSet);
								if (!result) {
									return result;
								}
							} else {
								return false;
							}
						} else {
							return false;
						}
						if (!result) {
							break;
						}

					} else {
						return false;
					}
				}
			} else {
				result = checkParameter(fieldClass, fType, checkSet);
				if (!result) {
					return result;
				}
			}
		}
		return true;
	}

	public static Object getDefParamArg(Class cl) {
		if (!cl.isPrimitive())
			return null;
		else if (boolean.class.equals(cl))
			return Boolean.FALSE;
		else if (byte.class.equals(cl))
			return new Byte((byte) 0);
		else if (short.class.equals(cl))
			return new Short((short) 0);
		else if (char.class.equals(cl))
			return new Character((char) 0);
		else if (int.class.equals(cl))
			return Integer.valueOf(0);
		else if (long.class.equals(cl))
			return Long.valueOf(0);
		else if (float.class.equals(cl))
			return Float.valueOf(0);
		else if (double.class.equals(cl))
			return Double.valueOf(0);
		else
			throw new UnsupportedOperationException();
	}
}