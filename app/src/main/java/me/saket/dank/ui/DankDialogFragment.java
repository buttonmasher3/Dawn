package me.saket.dank.ui;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import me.saket.dank.utils.lifecycle.LifecycleOwnerDialogFragment;

/**
 * Base class for fragments.
 */
public class DankDialogFragment extends LifecycleOwnerDialogFragment {

  private CompositeDisposable onStopDisposables;
  private CompositeDisposable onDestroyDisposables;

  @Override
  public void onStop() {
    if (onStopDisposables != null) {
      onStopDisposables.clear();
    }

    super.onStop();
  }

  @Override
  public void onDestroy() {
    if (onDestroyDisposables != null) {
      onDestroyDisposables.clear();
    }

    super.onDestroy();
  }

  protected void unsubscribeOnStop(Disposable subscription) {
    if (onStopDisposables == null) {
      onStopDisposables = new CompositeDisposable();
    }
    onStopDisposables.add(subscription);
  }

  protected void unsubscribeOnDestroy(Disposable subscription) {
    if (onDestroyDisposables == null) {
      onDestroyDisposables = new CompositeDisposable();
    }
    onDestroyDisposables.add(subscription);
  }
}
