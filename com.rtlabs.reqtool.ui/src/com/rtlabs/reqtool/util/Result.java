package com.rtlabs.reqtool.util;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;

import com.google.common.collect.ImmutableList;
import com.rtlabs.reqtool.ui.Activator;

/**
 * Used to return a value from some operation, together with status information about the operation.
 * 
 * This is useful in the following scenarios:
 * - Returning the result from an operation which may fail with an error message.
 * - Returning a result together with information and/or warnings from the operation. 
 * 
 * @param <T> The type of the result value
 */
public class Result<T> {
	private final T result;
	private final List<IStatus> statuses;

	public Result(T result, List<IStatus> statuses) {
		this.result = result;
		this.statuses = Objects.requireNonNull(statuses);
	}

	public Result(T result) {
		this(result, Collections.emptyList());
	}

	public static <T> Result<T> success(T result) {
		return new Result<>(result, emptyList());
	}
	
	public static <T> Result<T> failure(String message) {
		return new Result<>(null, ImmutableList.of(ValidationStatus.error(message)));
	}
	
	public static <T> Result<T> failure(List<IStatus> statuses) {
		return new Result<>(null, statuses);
	}
	
	public boolean isNoErrors() {
		return isAllLessSevere(IStatus.ERROR);
	}

	public boolean isNoWarnings() {
		return isAllLessSevere(IStatus.WARNING);
	}
	
	public boolean isAllOk() {
		return isAllLessSevere(IStatus.INFO);
	}
	
	public IStatus toMultiStatus(String message) {
		return getStatuses().size() == 1
			? getStatuses().get(0)
			: new MultiStatus(Activator.PLUGIN_ID, -1, getStatuses().toArray(new MultiStatus[0]), message, null);
	}
	
	public List<String> getAllMessages() {
		return statuses.stream().map(s -> s.getMessage()).collect(toList());
	}

	public boolean isAllLessSevere(int severity) {
		return statuses.stream().allMatch(s -> s.getSeverity() < severity);
	}
	
	public T getResult() {
		return result;
	}

	public List<IStatus> getStatuses() {
		return statuses;
	}
	
	public String toString() {
		return getClass().getSimpleName() + "<" + result + ", " + statuses;
	}
	
	public Optional<T> toOptional() {
		return Optional.ofNullable(getResult());
	}

	public String getMessage() {
		if (statuses.size() > 1) {
			throw new IllegalStateException();
		}
		
		return statuses.get(0).getMessage();
	}
}
