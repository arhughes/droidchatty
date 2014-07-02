package cc.hughes.droidchatty2;

import android.app.Activity;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.view.View;

import java.lang.reflect.Field;

public class ViewInjector {

    public static void inject(Activity activity) {
        // could possibly grab the root view and use that instead of the Activity
        ViewFinder finder = new ViewFinder(activity);
        inject(activity, finder);
    }

    public static void inject(Fragment fragment) {
        ViewFinder finder = new ViewFinder(fragment.getView());
        inject(fragment, finder);
    }

    public static void inject(Object patient, View view) {
        ViewFinder finder = new ViewFinder(view);
        inject(patient, finder);
    }

    private static void inject(Object patient, ViewFinder finder) {

        // see if any fields of the patient need an injection
        Class<?> c = patient.getClass();
        for (Field field : c.getDeclaredFields()) {
            if (!field.isAnnotationPresent(ViewInjected.class)) continue;
            if (!View.class.isAssignableFrom(field.getType())) continue;

            try {
                // find the correct view, and inject it into the patient
                ViewInjected injected = field.getAnnotation(ViewInjected.class);
                View view = finder.findViewById(injected.value());

                //if (view == null)
                //    throw new Exception("Could not find view with id `" + finder.getResources().getResourceName(injected.value()) + "`");

                if (view != null) {
                    field.setAccessible(true);
                    field.set(patient, view);
                }
            } catch (Exception ex) {
                throw new ViewInjectorException("Error injecting `" + field.getName() + "`: " + ex.getMessage(), ex);
            }
        }
    }

    private static class ViewFinder {
        Activity mActivity;
        View mView;

        public ViewFinder(Activity activity) {
            mActivity = activity;
        }

        public ViewFinder(View view) {
            mView = view;
        }

        public View findViewById(int id) {
            // Activity and View both have a findViewById method, but share no base class
            if (mActivity != null)
                return mActivity.findViewById(id);
            else
                return mView.findViewById(id);
        }

        public Resources getResources() {
            // Activity and View both have a getResources method, but share no base class
            if (mActivity != null)
                return mActivity.getResources();
            else
                return mView.getResources();
        }

    }

}
