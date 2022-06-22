package org.nanotek.crawler.data.util.buddy;

import java.lang.annotation.Annotation;

import org.nanotek.crawler.Base;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import net.bytebuddy.ByteBuddy;

public class BuddyBase{

	public BuddyBase() {
	}


	@SuppressWarnings("deprecation")
	public  Class<?> generateEmptySuperClass()
	{
		Class<?> s;
		try {
			s = new ByteBuddy()
					.subclass(Base.class.asSubclass(Base.class))
					.annotateType(new DataAnnotation() , new NoArgConstructorAnnotation())
					.make()
					.load(Thread.currentThread().getContextClassLoader())
					.getLoaded();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		};
		return s;
	}

	public  Class<?> addField(Class<?> base , Class<?> methodc , String methodName) {
		Class<?> c;
		try {
			c = new ByteBuddy()
					.subclass(base) 
					.defineProperty(methodName, methodc)
					.annotateField(new AccessorsAnnotation())
					.make()
					.load(base.getClassLoader())
					.getLoaded();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return c;
	}

	class DataAnnotation implements Data{

		@Override
		public Class<? extends Annotation> annotationType() {
			return Data.class;
		}

		@Override
		public String staticConstructor() {
			return "";
		}
	}
	
	class NoArgConstructorAnnotation implements NoArgsConstructor{

		@Override
		public Class<? extends Annotation> annotationType() {
			return NoArgsConstructor.class;
		}

		@Override
		public String staticName() {
			return "NoArgConstructorAnnotation";
		}

		@Override
		public AnyAnnotation[] onConstructor() {
			return null;
		}

		@Override
		public AccessLevel access() {
			return AccessLevel.PUBLIC;
		}

		@Override
		public boolean force() {
			return false;
		}
		
	}
	
	class AccessorsAnnotation implements Accessors {

		@Override
		public Class<? extends Annotation> annotationType() {
			return Accessors.class;
		}

		@Override
		public boolean fluent() {
			return false;
		}

		@Override
		public boolean chain() {
			return false;
		}

		@Override
		public boolean makeFinal() {
			return false;
		}

		@Override
		public String[] prefix() {
			return null;
		}
		
	}
	

}
