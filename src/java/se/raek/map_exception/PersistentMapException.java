package se.raek.map_exception;

import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;

public final class PersistentMapException extends RuntimeException {
	
	private IPersistentMap map;
	
	public PersistentMapException(IPersistentMap map) {
		this.map = map;
		
		StackTraceElement[] stackTrace = (StackTraceElement[]) map.valAt(Keyword.intern("stack-trace"));
		if (stackTrace != null) {
			setStackTrace(stackTrace);
		}
	}
	
	public IPersistentMap getMap() {
		return map.assoc(Keyword.intern("stack-trace"), getStackTrace());
	}
	
	public String getMessage() {
		return (String) map.valAt(Keyword.intern("message"));
	}
	
	public Throwable getCause() {
		return (Throwable) map.valAt(Keyword.intern("cause"));
	}
	
	public Throwable initCause(Throwable cause) {
		if (cause == this) {
			throw new IllegalArgumentException("A throwable cannot be its own cause.");
		}
		Keyword causeKeyword = Keyword.intern("cause");
		if (map.entryAt(causeKeyword) == null) {
			throw new IllegalStateException("The cause of this throwable has already been initialized.");
		}
		map = map.assoc(causeKeyword, cause);
		return this;
	}
	
	public String toString() {
		Object type = map.valAt(Keyword.intern("type"));
		if (type == null) {
			return getClass().getName() + ": " + getMessage();
		} else {
			return getClass().getName() + ": " + type.toString() + ": " + getMessage();
		}
	}
	
}
