/**
 * Eu.inloop Copyright(C) 2014
 * Modifications Copyright(C) 2015 Fred Grott(GrottWorkShop)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under License.
 */
package com.github.shareme.gwsnuclearfusion.library.viewmodel;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.shareme.gwsnuclearfusion.library.view.IView;

import java.util.UUID;

import timber.log.Timber;

/**
 * ViewModelHelper
 * Created by fgrott on 12/19/2015.
 */
@SuppressWarnings("unused")
public class ViewModelHelper<T extends IView, R extends AbstractViewModel<T>> {

    private String mScreenId;
    private R mViewModel;

    /**
     *
     * @param activity the activity
     * @param savedInstanceState the savedInstanceState
     * @param viewModelClass the viewModelClass
     */
    @SuppressWarnings("unchecked")
    public void onCreate(@NonNull Activity activity, @Nullable Bundle savedInstanceState,
                         @NonNull Class<? extends AbstractViewModel<T>> viewModelClass) {
        if (savedInstanceState == null) {
            mScreenId = UUID.randomUUID().toString();
        } else {
            mScreenId = savedInstanceState.getString("identifier");
        }

        final ViewModelProvider.ViewModelWrapper<T> viewModelWrapper = getViewModelProvider(activity).getViewModelProvider().getViewModel(mScreenId, viewModelClass);
        mViewModel = (R) viewModelWrapper.viewModel;
        if (savedInstanceState != null && viewModelWrapper.wasCreated) {
            Timber.e("model", "Application recreated by system - restoring viewmodel");
            mViewModel.restoreState(savedInstanceState);
        }
    }

    /**
     *
     * @param view the view
     */
    public void initWithView(T view) {
        if (mViewModel == null) {
            //no viewmodel for this fragment
            return;
        }
        mViewModel.initWithView(view);
    }

    /**
     *
     * @param fragment the fragment
     */
    public void onDestroyView(@NonNull Fragment fragment) {
        if (mViewModel == null) {
            //no viewmodel for this fragment
            return;
        }
        mViewModel.clearView();
        if (fragment.getActivity() != null && fragment.getActivity().isFinishing()) {
            removeViewModel(fragment.getActivity());
        }
    }

    /**
     *
     * @param fragment the fragment
     */
    public void onDestroy(@NonNull Fragment fragment) {
        if (mViewModel == null) {
            //no viewmodel for this fragment
            return;
        }
        if (fragment.getActivity().isFinishing()) {
            removeViewModel(fragment.getActivity());
        } else if (fragment.isRemoving()) {
            Timber.d("mode", "Removing viewmodel - fragment replaced");
            removeViewModel(fragment.getActivity());
        }
    }

    /**
     *
     * @param activity the activity
     */
    public void onDestroy(@NonNull Activity activity) {
        if (mViewModel == null) {
            //no viewmodel for this fragment
            return;
        }
        if (activity.isFinishing()) {
            removeViewModel(activity);
        }
    }

    public void onStop() {
        if (mViewModel == null) {
            //no viewmodel for this fragment
            return;
        }
        mViewModel.onStop();
    }

    public void onStart() {
        if (mViewModel == null) {
            //no viewmodel for this fragment
            return;
        }
        mViewModel.onStart();
    }

    public R getViewModel() {
        return mViewModel;
    }

    /**
     *
     * @param bundle the bundle
     */
    public void onSaveInstanceState(@Nullable Bundle bundle) {
        if (bundle != null) {
            bundle.putString("identifier", mScreenId);
        }
        if (mViewModel != null) {
            mViewModel.saveState(bundle);
        }
    }

    /**
     *
     * @param activity the activity
     * @return the IViewModelProvider
     */
    private IViewModelProvider getViewModelProvider(@NonNull Activity activity) {
        if (!(activity instanceof IViewModelProvider)) {
            throw new IllegalStateException("Your activity must implement IViewModelProvider");
        }
        return ((IViewModelProvider)activity);
    }

    /**
     *
     * @param activity the activity
     * @return boolean removed
     */
    protected boolean removeViewModel(@NonNull Activity activity) {
        boolean removed = getViewModelProvider(activity).getViewModelProvider().remove(mScreenId);
        mViewModel.onModelRemoved();
        return removed;
    }
}
