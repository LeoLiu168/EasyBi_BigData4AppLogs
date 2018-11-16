package com.easybi.leo.utils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/*
 * 属性复制的工具类
 */
public class PropertiesUtil {
	/*
	 * 通过内省进行属性复制(对象到对象)
	 */
	public static void copyProperties(Object src, Object dest) {
		
		try {
			//源对象的BeanInfo
			BeanInfo srcBeanInfo = Introspector.getBeanInfo(src.getClass());
			//获取属性描述符
			PropertyDescriptor[] descriptors = srcBeanInfo.getPropertyDescriptors();
			for (PropertyDescriptor descriptor : descriptors) {
				//获取getter和setter方法
				Method getter = descriptor.getReadMethod();
				Method setter = descriptor.getWriteMethod();
				//获取set方法名称
				String setterName = setter.getName();
				//获取setter方法参数
				Class<?>[] parameterTypes = setter.getParameterTypes();
				
				Object value = getter.invoke(src);
				
				try {
					Method destSetter = dest.getClass().getMethod(setterName, parameterTypes);
					destSetter.invoke(dest, value);
				} catch (Exception e) {
					continue;
				} 
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/*
	 * 复制对象属性至一个数组的重载方法
	 */
	public static void copyProperties(Object src, Object[] arr) {
		for (Object obj : arr) {
			copyProperties(src, obj);
		}
	}

}
