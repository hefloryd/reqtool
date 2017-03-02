package com.rtlabs.common.util;

import java.util.HashMap;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.databinding.observable.DisposeEvent;
import org.eclipse.core.databinding.observable.IDecoratingObservable;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObserving;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.ISWTObservable;
import org.eclipse.jface.databinding.viewers.IViewerObservable;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.widgets.Form;

/**
 * Utility class for working with data binding. 
 * 
 * This class is copied from https://bugs.eclipse.org/bugs/show_bug.cgi?id=219661 .
 *
 * @author Matt Biggs
 */
public class MessageManagerSupport {
	protected final Form form;
	protected final IMessageManager messageManager;
	protected final DataBindingContext dataBindingContext;
	protected final HashMap<ValidationStatusProvider, IValueChangeListener<IStatus>> valueChangeListeners;

	protected final IListChangeListener<ValidationStatusProvider> listChangeListener = 
			new IListChangeListener<ValidationStatusProvider>() {			
		@Override
		public void handleListChange(ListChangeEvent<? extends ValidationStatusProvider> event) {
			ListDiffVisitor<ValidationStatusProvider> listDiffVisitor = new ListDiffVisitor<ValidationStatusProvider>() {								
				@Override
				public void handleRemove(int index, ValidationStatusProvider element) {
					removeValueChangeListener(element);
				}
				
				@Override
				public void handleAdd(int index, ValidationStatusProvider element) {
					addValueChangeListener(element);
				}
			};
			
			@SuppressWarnings("unchecked")
			ListDiff<ValidationStatusProvider> diff = ((ListChangeEvent<ValidationStatusProvider>) event).diff;
			diff.accept( listDiffVisitor);
		}
		
	};
	
	protected final IDisposeListener observableDisposeListener = new IDisposeListener() {
		public void handleDispose(DisposeEvent staleEvent) {
			dispose();
		}
	};
	
	protected final DisposeListener formDisposeListener = new DisposeListener() {		
		@Override
		public void widgetDisposed(org.eclipse.swt.events.DisposeEvent e) {
			dispose();
		}
	};
	
	protected final class ValueChangeListener implements IValueChangeListener<IStatus> {
		private final ValidationStatusProvider provider;
		
		public ValueChangeListener(ValidationStatusProvider provider) {
			this.provider = provider;
		}		
		@Override
		public void handleValueChange(ValueChangeEvent<? extends IStatus> event) {
			@SuppressWarnings("unchecked")
			IObservableList<IObservable> targets = provider.getTargets();
			handleStatusChanged(targets, event.diff.getOldValue(), event.diff.getNewValue());
		}
	}
	
	public static MessageManagerSupport create(Form form, DataBindingContext context) {
		return new MessageManagerSupport(form, context);
	}
	
	protected MessageManagerSupport(Form form, DataBindingContext dataBindingContext) {
		this.form = form;
		this.messageManager = form.getMessageManager();
		this.dataBindingContext = dataBindingContext;		
		this.valueChangeListeners = new HashMap<ValidationStatusProvider, IValueChangeListener<IStatus>>();
		
		init();
	}
	
	protected void init() {
		
		// Listen to the Form being disposed
		getForm().addDisposeListener(formDisposeListener);
		
		// Listen to any new ValidationStatusProviders
		@SuppressWarnings("unchecked")
		IObservableList<ValidationStatusProvider> statusProviders = getDataBindingContext().getValidationStatusProviders();

		statusProviders.addDisposeListener(observableDisposeListener);
		statusProviders.addListChangeListener(listChangeListener);		
		
		// Listen to changes to any existing ValidationStatusProviders
		for( Object element : statusProviders ) {	
			final ValidationStatusProvider observable = (ValidationStatusProvider)element;
			addValueChangeListener(observable);
		}

	}

	protected void handleStatusChanged(IObservableList<IObservable> target, final IStatus oldStatus, final IStatus newStatus) {
		final boolean isAutoUpdate = getMessageManager().isAutoUpdate();
		try {
			final Control control = findControl(target);
			
			// Disable update
			if( isAutoUpdate ) {
				getMessageManager().setAutoUpdate(false);
			}
			
			// Remove the old status
			if( control != null ) {
				getMessageManager().removeMessage(oldStatus, control);
			} else {
				getMessageManager().removeMessage(oldStatus);
			}
			
			// Add new status if it is a validation failure
			if( !newStatus.isOK() ) {
				if( control != null ) {
					getMessageManager().addMessage(newStatus, getDescriptionText(newStatus), newStatus, getStatusType(newStatus), control);
				} else {
					getMessageManager().addMessage(newStatus, getDescriptionText(newStatus), newStatus, getStatusType(newStatus));
				}
			}
			
		} finally {
			// Re-enable update
			if( isAutoUpdate ) {
				getMessageManager().setAutoUpdate(true);
			}
		}
	}

	protected void addValueChangeListener(ValidationStatusProvider observable) {
		final IValueChangeListener<IStatus> valueChangeListener = new ValueChangeListener(observable);
		
		@SuppressWarnings("unchecked")
		final IObservableValue<IStatus> value = observable.getValidationStatus();
		value.addDisposeListener(observableDisposeListener);
		value.addValueChangeListener(valueChangeListener);
		
		valueChangeListeners.put(observable, valueChangeListener);
	}
	
	protected void removeValueChangeListener(ValidationStatusProvider observable) {				
		IValueChangeListener<IStatus> valueChangeListener = valueChangeListeners.get(observable);
		
		@SuppressWarnings("unchecked")
		final IObservableValue<IStatus> value = observable.getValidationStatus();
		value.removeDisposeListener(observableDisposeListener);
		value.removeValueChangeListener(valueChangeListener);
		
		valueChangeListeners.remove(observable);
	}
	
	protected Control findControl(IObservable target) {
		if( target instanceof IObservableList ) {
			IObservableList<?> list = (IObservableList<?>) target;
			for( int i = 0; i < list.size(); i++ ) {
				Control control = findControl((IObservable) list.get(i));
				if( control != null )
					return control;
			}				
		}
		
		if (target instanceof ISWTObservable) {
			Widget widget = ((ISWTObservable) target).getWidget();
			if (widget instanceof Control)
				return (Control) widget;
		}

		if (target instanceof IViewerObservable) {
			Viewer viewer = ((IViewerObservable) target).getViewer();
			return viewer.getControl();
		}

		if (target instanceof IDecoratingObservable) {
			IObservable decorated = ((IDecoratingObservable) target).getDecorated();
			Control control = findControl(decorated);
			if (control != null)
				return control;
		}

		if (target instanceof IObserving) {
			Object observed = ((IObserving) target).getObserved();
			if (observed instanceof IObservable)
				return findControl((IObservable) observed);
		}

		return null;
	}
	
	public void dispose() {
		// Remove Form dispose listener
		getForm().removeDisposeListener(formDisposeListener);
		
		// Remove notifications of changes to the values
		@SuppressWarnings("unchecked")
		IObservableList<ValidationStatusProvider> statusProviders = getDataBindingContext().getValidationStatusProviders();
		
		for (ValidationStatusProvider observable : statusProviders ) {
			removeValueChangeListener(observable);
		}
		
		// Remove notification of changes to the ValidationStatusProviders
		statusProviders.removeDisposeListener(observableDisposeListener);
		statusProviders.removeListChangeListener(listChangeListener);
	}
	
	@SuppressWarnings("static-method")
	private static String getDescriptionText(IStatus status) {
		if (status == null) return "";
		if (!status.isMultiStatus()) return status.getMessage();

		StringBuilder message = new StringBuilder();
		String sep = "";
		for (IStatus s : status.getChildren()) {
			message.append(sep);
			sep = System.lineSeparator();
			message.append(getDescriptionText(s));
		}
		return message.toString();
	}

	@SuppressWarnings("static-method")
	protected int getStatusType(IStatus status) {
		switch (status.getSeverity()) {
		case IStatus.OK:
			return IMessageProvider.NONE;
		case IStatus.CANCEL:
			return IMessageProvider.NONE;
		case IStatus.INFO:
			return IMessageProvider.INFORMATION;
		case IStatus.WARNING:
			return IMessageProvider.WARNING;
		case IStatus.ERROR:
			return IMessageProvider.ERROR;
		default:                       
			return IMessageProvider.NONE;
		}
	}

	protected Form getForm() {
		return form;
	}
	
	protected IMessageManager getMessageManager() {
		return messageManager;
	}

	protected DataBindingContext getDataBindingContext() {
		return dataBindingContext;
	}
}
