package androidx.fragment.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import androidx.collection.ArrayMap;
import androidx.core.app.SharedElementCallback;
import androidx.core.os.CancellationSignal;
import androidx.core.util.Preconditions;
import androidx.core.view.OneShotPreDrawListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewGroupCompat;
import androidx.fragment.app.FragmentAnim;
import androidx.fragment.app.SpecialEffectsController;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* loaded from: classes.dex */
class DefaultSpecialEffectsController extends SpecialEffectsController {
    DefaultSpecialEffectsController(ViewGroup container) {
        super(container);
    }

    /* JADX WARN: Removed duplicated region for block: B:28:0x00b4  */
    @Override // androidx.fragment.app.SpecialEffectsController
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    void executeOperations(java.util.List<androidx.fragment.app.SpecialEffectsController.Operation> r20, boolean r21) {
        /*
            Method dump skipped, instructions count: 294
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.fragment.app.DefaultSpecialEffectsController.executeOperations(java.util.List, boolean):void");
    }

    private void syncAnimations(List<SpecialEffectsController.Operation> operations) {
        Fragment lastOpFragment = operations.get(operations.size() - 1).getFragment();
        for (SpecialEffectsController.Operation operation : operations) {
            operation.getFragment().mAnimationInfo.mEnterAnim = lastOpFragment.mAnimationInfo.mEnterAnim;
            operation.getFragment().mAnimationInfo.mExitAnim = lastOpFragment.mAnimationInfo.mExitAnim;
            operation.getFragment().mAnimationInfo.mPopEnterAnim = lastOpFragment.mAnimationInfo.mPopEnterAnim;
            operation.getFragment().mAnimationInfo.mPopExitAnim = lastOpFragment.mAnimationInfo.mPopExitAnim;
        }
    }

    private void startAnimations(List<AnimationInfo> animationInfos, List<SpecialEffectsController.Operation> awaitingContainerChanges, boolean startedAnyTransition, Map<SpecialEffectsController.Operation, Boolean> startedTransitions) {
        int i;
        final AnimationInfo animationInfo;
        final View viewToAnimate;
        int i2;
        SpecialEffectsController.Operation operation;
        ViewGroup container = getContainer();
        Context context = container.getContext();
        ArrayList<AnimationInfo> animationsToRun = new ArrayList<>();
        Iterator<AnimationInfo> it = animationInfos.iterator();
        boolean startedAnyAnimator = false;
        while (true) {
            boolean startedAnyAnimator2 = it.hasNext();
            i = 2;
            if (!startedAnyAnimator2) {
                break;
            }
            final AnimationInfo animationInfo2 = it.next();
            if (animationInfo2.isVisibilityUnchanged()) {
                animationInfo2.completeSpecialEffect();
            } else {
                FragmentAnim.AnimationOrAnimator anim = animationInfo2.getAnimation(context);
                if (anim == null) {
                    animationInfo2.completeSpecialEffect();
                } else {
                    final Animator animator = anim.animator;
                    if (animator == null) {
                        animationsToRun.add(animationInfo2);
                    } else {
                        final SpecialEffectsController.Operation operation2 = animationInfo2.getOperation();
                        Fragment fragment = operation2.getFragment();
                        boolean startedTransition = Boolean.TRUE.equals(startedTransitions.get(operation2));
                        if (startedTransition) {
                            if (FragmentManager.isLoggingEnabled(2)) {
                                Log.v(FragmentManager.TAG, "Ignoring Animator set on " + fragment + " as this Fragment was involved in a Transition.");
                            }
                            animationInfo2.completeSpecialEffect();
                        } else {
                            final boolean isHideOperation = operation2.getFinalState() == SpecialEffectsController.Operation.State.GONE;
                            if (isHideOperation) {
                                awaitingContainerChanges.remove(operation2);
                            }
                            final View viewToAnimate2 = fragment.mView;
                            container.startViewTransition(viewToAnimate2);
                            final ViewGroup container2 = container;
                            animator.addListener(new AnimatorListenerAdapter() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.2
                                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                                public void onAnimationEnd(Animator anim2) {
                                    container2.endViewTransition(viewToAnimate2);
                                    if (isHideOperation) {
                                        operation2.getFinalState().applyState(viewToAnimate2);
                                    }
                                    animationInfo2.completeSpecialEffect();
                                    if (FragmentManager.isLoggingEnabled(2)) {
                                        Log.v(FragmentManager.TAG, "Animator from operation " + operation2 + " has ended.");
                                    }
                                }
                            });
                            animator.setTarget(viewToAnimate2);
                            animator.start();
                            if (FragmentManager.isLoggingEnabled(2)) {
                                Log.v(FragmentManager.TAG, "Animator from operation " + operation2 + " has started.");
                            }
                            CancellationSignal signal = animationInfo2.getSignal();
                            signal.setOnCancelListener(new CancellationSignal.OnCancelListener() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.3
                                @Override // androidx.core.os.CancellationSignal.OnCancelListener
                                public void onCancel() {
                                    animator.end();
                                    if (FragmentManager.isLoggingEnabled(2)) {
                                        Log.v(FragmentManager.TAG, "Animator from operation " + operation2 + " has been canceled.");
                                    }
                                }
                            });
                            container = container2;
                            startedAnyAnimator = true;
                        }
                    }
                }
            }
        }
        ViewGroup container3 = container;
        Iterator<AnimationInfo> it2 = animationsToRun.iterator();
        while (it2.hasNext()) {
            AnimationInfo animationInfo3 = it2.next();
            final SpecialEffectsController.Operation operation3 = animationInfo3.getOperation();
            Fragment fragment2 = operation3.getFragment();
            if (startedAnyTransition) {
                if (FragmentManager.isLoggingEnabled(i)) {
                    Log.v(FragmentManager.TAG, "Ignoring Animation set on " + fragment2 + " as Animations cannot run alongside Transitions.");
                }
                animationInfo3.completeSpecialEffect();
            } else if (startedAnyAnimator) {
                if (FragmentManager.isLoggingEnabled(i)) {
                    Log.v(FragmentManager.TAG, "Ignoring Animation set on " + fragment2 + " as Animations cannot run alongside Animators.");
                }
                animationInfo3.completeSpecialEffect();
            } else {
                View viewToAnimate3 = fragment2.mView;
                Animation anim2 = (Animation) Preconditions.checkNotNull(((FragmentAnim.AnimationOrAnimator) Preconditions.checkNotNull(animationInfo3.getAnimation(context))).animation);
                SpecialEffectsController.Operation.State finalState = operation3.getFinalState();
                if (finalState != SpecialEffectsController.Operation.State.REMOVED) {
                    viewToAnimate3.startAnimation(anim2);
                    animationInfo3.completeSpecialEffect();
                    viewToAnimate = viewToAnimate3;
                    operation = operation3;
                    animationInfo = animationInfo3;
                    i2 = i;
                } else {
                    container3.startViewTransition(viewToAnimate3);
                    Animation animation = new FragmentAnim.EndViewTransitionAnimation(anim2, container3, viewToAnimate3);
                    final ViewGroup container4 = container3;
                    animationInfo = animationInfo3;
                    viewToAnimate = viewToAnimate3;
                    i2 = i;
                    operation = operation3;
                    container3 = container4;
                    animation.setAnimationListener(new Animation.AnimationListener() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.4
                        @Override // android.view.animation.Animation.AnimationListener
                        public void onAnimationStart(Animation animation2) {
                            if (FragmentManager.isLoggingEnabled(2)) {
                                Log.v(FragmentManager.TAG, "Animation from operation " + operation3 + " has reached onAnimationStart.");
                            }
                        }

                        @Override // android.view.animation.Animation.AnimationListener
                        public void onAnimationEnd(Animation animation2) {
                            container4.post(new Runnable() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.4.1
                                @Override // java.lang.Runnable
                                public void run() {
                                    container4.endViewTransition(viewToAnimate);
                                    animationInfo.completeSpecialEffect();
                                }
                            });
                            if (FragmentManager.isLoggingEnabled(2)) {
                                Log.v(FragmentManager.TAG, "Animation from operation " + operation3 + " has ended.");
                            }
                        }

                        @Override // android.view.animation.Animation.AnimationListener
                        public void onAnimationRepeat(Animation animation2) {
                        }
                    });
                    viewToAnimate.startAnimation(animation);
                    if (FragmentManager.isLoggingEnabled(i2)) {
                        Log.v(FragmentManager.TAG, "Animation from operation " + operation + " has started.");
                    }
                }
                CancellationSignal signal2 = animationInfo.getSignal();
                final ViewGroup container5 = container3;
                final View viewToAnimate4 = viewToAnimate;
                final AnimationInfo animationInfo4 = animationInfo;
                final SpecialEffectsController.Operation operation4 = operation;
                CancellationSignal.OnCancelListener onCancelListener = new CancellationSignal.OnCancelListener() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.5
                    @Override // androidx.core.os.CancellationSignal.OnCancelListener
                    public void onCancel() {
                        viewToAnimate4.clearAnimation();
                        container5.endViewTransition(viewToAnimate4);
                        animationInfo4.completeSpecialEffect();
                        if (FragmentManager.isLoggingEnabled(2)) {
                            Log.v(FragmentManager.TAG, "Animation from operation " + operation4 + " has been cancelled.");
                        }
                    }
                };
                container3 = container5;
                signal2.setOnCancelListener(onCancelListener);
                i = i2;
            }
        }
    }

    private Map<SpecialEffectsController.Operation, Boolean> startTransitions(List<TransitionInfo> transitionInfos, List<SpecialEffectsController.Operation> awaitingContainerChanges, boolean isPop, SpecialEffectsController.Operation firstOut, SpecialEffectsController.Operation lastIn) {
        String str;
        Object mergedNonOverlappingTransition;
        View firstOutEpicenterView;
        String str2;
        Map<SpecialEffectsController.Operation, Boolean> startedTransitions;
        Rect lastInEpicenterRect;
        Object transition;
        ArrayList<View> sharedElementLastInViews;
        final ArrayList<View> transitioningViews;
        ArrayList<View> sharedElementFirstOutViews;
        ArrayList<View> sharedElementFirstOutViews2;
        String str3;
        Object mergedTransition;
        View nonExistentView;
        View firstOutEpicenterView2;
        Map<SpecialEffectsController.Operation, Boolean> startedTransitions2;
        Object mergedNonOverlappingTransition2;
        SpecialEffectsController.Operation operation;
        ArrayList<View> enteringViews;
        View nonExistentView2;
        Rect lastInEpicenterRect2;
        ArrayList<View> sharedElementFirstOutViews3;
        ArrayMap<String, String> sharedElementNameMapping;
        ArrayList<View> sharedElementLastInViews2;
        Map<SpecialEffectsController.Operation, Boolean> startedTransitions3;
        Map<SpecialEffectsController.Operation, Boolean> startedTransitions4;
        SharedElementCallback exitingCallback;
        SharedElementCallback exitingCallback2;
        Rect lastInEpicenterRect3;
        ArrayList<String> exitingNames;
        ArrayList<String> exitingNames2;
        final Rect lastInEpicenterRect4;
        SharedElementCallback enteringCallback;
        ArrayList<String> exitingNames3;
        final boolean z = isPop;
        final SpecialEffectsController.Operation operation2 = firstOut;
        final SpecialEffectsController.Operation operation3 = lastIn;
        Map<SpecialEffectsController.Operation, Boolean> startedTransitions5 = new HashMap<>();
        FragmentTransitionImpl transitionImpl = null;
        for (TransitionInfo transitionInfo : transitionInfos) {
            if (!transitionInfo.isVisibilityUnchanged()) {
                FragmentTransitionImpl handlingImpl = transitionInfo.getHandlingImpl();
                if (transitionImpl == null) {
                    transitionImpl = handlingImpl;
                } else if (handlingImpl != null && transitionImpl != handlingImpl) {
                    throw new IllegalArgumentException("Mixing framework transitions and AndroidX transitions is not allowed. Fragment " + transitionInfo.getOperation().getFragment() + " returned Transition " + transitionInfo.getTransition() + " which uses a different Transition  type than other Fragments.");
                }
            }
        }
        if (transitionImpl == null) {
            for (TransitionInfo transitionInfo2 : transitionInfos) {
                startedTransitions5.put(transitionInfo2.getOperation(), false);
                transitionInfo2.completeSpecialEffect();
            }
            return startedTransitions5;
        }
        View nonExistentView3 = new View(getContainer().getContext());
        Object sharedElementTransition = null;
        Rect lastInEpicenterRect5 = new Rect();
        ArrayList<View> sharedElementFirstOutViews4 = new ArrayList<>();
        ArrayList<View> sharedElementLastInViews3 = new ArrayList<>();
        ArrayMap<String, String> sharedElementNameMapping2 = new ArrayMap<>();
        Iterator<TransitionInfo> it = transitionInfos.iterator();
        boolean hasLastInEpicenter = false;
        View firstOutEpicenterView3 = null;
        while (true) {
            boolean zHasNext = it.hasNext();
            str = FragmentManager.TAG;
            if (!zHasNext) {
                break;
            }
            TransitionInfo transitionInfo3 = it.next();
            boolean hasSharedElementTransition = transitionInfo3.hasSharedElementTransition();
            if (!hasSharedElementTransition || operation2 == null || operation3 == null) {
                nonExistentView2 = nonExistentView3;
                lastInEpicenterRect2 = lastInEpicenterRect5;
                sharedElementFirstOutViews3 = sharedElementFirstOutViews4;
                sharedElementNameMapping = sharedElementNameMapping2;
                sharedElementLastInViews2 = sharedElementLastInViews3;
                startedTransitions3 = startedTransitions5;
                firstOutEpicenterView3 = firstOutEpicenterView3;
            } else {
                Object sharedElementTransition2 = transitionImpl.wrapTransitionInSet(transitionImpl.cloneTransition(transitionInfo3.getSharedElementTransition()));
                ArrayList<String> exitingNames4 = operation3.getFragment().getSharedElementSourceNames();
                View firstOutEpicenterView4 = firstOutEpicenterView3;
                ArrayList<String> firstOutSourceNames = operation2.getFragment().getSharedElementSourceNames();
                ArrayList<String> firstOutTargetNames = operation2.getFragment().getSharedElementTargetNames();
                int index = 0;
                while (true) {
                    startedTransitions4 = startedTransitions5;
                    if (index >= firstOutTargetNames.size()) {
                        break;
                    }
                    int nameIndex = exitingNames4.indexOf(firstOutTargetNames.get(index));
                    ArrayList<String> firstOutTargetNames2 = firstOutTargetNames;
                    if (nameIndex != -1) {
                        exitingNames4.set(nameIndex, firstOutSourceNames.get(index));
                    }
                    index++;
                    startedTransitions5 = startedTransitions4;
                    firstOutTargetNames = firstOutTargetNames2;
                }
                ArrayList<String> enteringNames = operation3.getFragment().getSharedElementTargetNames();
                if (!z) {
                    SharedElementCallback exitingCallback3 = operation2.getFragment().getExitTransitionCallback();
                    exitingCallback = exitingCallback3;
                    exitingCallback2 = operation3.getFragment().getEnterTransitionCallback();
                } else {
                    SharedElementCallback exitingCallback4 = operation2.getFragment().getEnterTransitionCallback();
                    exitingCallback = exitingCallback4;
                    exitingCallback2 = operation3.getFragment().getExitTransitionCallback();
                }
                int numSharedElements = exitingNames4.size();
                View nonExistentView4 = nonExistentView3;
                int i = 0;
                while (i < numSharedElements) {
                    int numSharedElements2 = numSharedElements;
                    String exitingName = exitingNames4.get(i);
                    int i2 = i;
                    String enteringName = enteringNames.get(i);
                    sharedElementNameMapping2.put(exitingName, enteringName);
                    i = i2 + 1;
                    numSharedElements = numSharedElements2;
                }
                if (!FragmentManager.isLoggingEnabled(2)) {
                    lastInEpicenterRect3 = lastInEpicenterRect5;
                } else {
                    Log.v(FragmentManager.TAG, ">>> entering view names <<<");
                    Iterator<String> it2 = enteringNames.iterator();
                    while (true) {
                        Iterator<String> it3 = it2;
                        if (!it2.hasNext()) {
                            break;
                        }
                        Log.v(FragmentManager.TAG, "Name: " + it3.next());
                        it2 = it3;
                        lastInEpicenterRect5 = lastInEpicenterRect5;
                    }
                    lastInEpicenterRect3 = lastInEpicenterRect5;
                    Log.v(FragmentManager.TAG, ">>> exiting view names <<<");
                    for (Iterator<String> it4 = exitingNames4.iterator(); it4.hasNext(); it4 = it4) {
                        Log.v(FragmentManager.TAG, "Name: " + it4.next());
                    }
                }
                ArrayMap<String, View> firstOutViews = new ArrayMap<>();
                findNamedViews(firstOutViews, operation2.getFragment().mView);
                firstOutViews.retainAll(exitingNames4);
                if (exitingCallback != null) {
                    if (FragmentManager.isLoggingEnabled(2)) {
                        Log.v(FragmentManager.TAG, "Executing exit callback for operation " + operation2);
                    }
                    exitingCallback.onMapSharedElements(exitingNames4, firstOutViews);
                    int i3 = exitingNames4.size() - 1;
                    while (i3 >= 0) {
                        String name = exitingNames4.get(i3);
                        View view = firstOutViews.get(name);
                        if (view == null) {
                            sharedElementNameMapping2.remove(name);
                            exitingNames3 = exitingNames4;
                        } else if (name.equals(ViewCompat.getTransitionName(view))) {
                            exitingNames3 = exitingNames4;
                        } else {
                            String targetValue = sharedElementNameMapping2.remove(name);
                            exitingNames3 = exitingNames4;
                            sharedElementNameMapping2.put(ViewCompat.getTransitionName(view), targetValue);
                        }
                        i3--;
                        exitingNames4 = exitingNames3;
                    }
                    exitingNames = exitingNames4;
                } else {
                    exitingNames = exitingNames4;
                    sharedElementNameMapping2.retainAll(firstOutViews.keySet());
                }
                final ArrayMap<String, View> lastInViews = new ArrayMap<>();
                findNamedViews(lastInViews, operation3.getFragment().mView);
                lastInViews.retainAll(enteringNames);
                lastInViews.retainAll(sharedElementNameMapping2.values());
                if (exitingCallback2 != null) {
                    if (FragmentManager.isLoggingEnabled(2)) {
                        Log.v(FragmentManager.TAG, "Executing enter callback for operation " + operation3);
                    }
                    exitingCallback2.onMapSharedElements(enteringNames, lastInViews);
                    int i4 = enteringNames.size() - 1;
                    while (i4 >= 0) {
                        String name2 = enteringNames.get(i4);
                        View view2 = lastInViews.get(name2);
                        if (view2 == null) {
                            String key = FragmentTransition.findKeyForValue(sharedElementNameMapping2, name2);
                            if (key != null) {
                                sharedElementNameMapping2.remove(key);
                            }
                            enteringCallback = exitingCallback2;
                        } else if (name2.equals(ViewCompat.getTransitionName(view2))) {
                            enteringCallback = exitingCallback2;
                        } else {
                            String key2 = FragmentTransition.findKeyForValue(sharedElementNameMapping2, name2);
                            if (key2 == null) {
                                enteringCallback = exitingCallback2;
                            } else {
                                enteringCallback = exitingCallback2;
                                sharedElementNameMapping2.put(key2, ViewCompat.getTransitionName(view2));
                            }
                        }
                        i4--;
                        exitingCallback2 = enteringCallback;
                    }
                } else {
                    FragmentTransition.retainValues(sharedElementNameMapping2, lastInViews);
                }
                retainMatchingViews(firstOutViews, sharedElementNameMapping2.keySet());
                retainMatchingViews(lastInViews, sharedElementNameMapping2.values());
                if (sharedElementNameMapping2.isEmpty()) {
                    sharedElementTransition = null;
                    sharedElementFirstOutViews4.clear();
                    sharedElementLastInViews3.clear();
                    operation2 = firstOut;
                    sharedElementFirstOutViews3 = sharedElementFirstOutViews4;
                    sharedElementNameMapping = sharedElementNameMapping2;
                    sharedElementLastInViews2 = sharedElementLastInViews3;
                    startedTransitions3 = startedTransitions4;
                    firstOutEpicenterView3 = firstOutEpicenterView4;
                    nonExistentView2 = nonExistentView4;
                    lastInEpicenterRect2 = lastInEpicenterRect3;
                } else {
                    FragmentTransition.callSharedElementStartEnd(operation3.getFragment(), firstOut.getFragment(), z, firstOutViews, true);
                    operation2 = firstOut;
                    OneShotPreDrawListener.add(getContainer(), new Runnable() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.6
                        @Override // java.lang.Runnable
                        public void run() {
                            FragmentTransition.callSharedElementStartEnd(operation3.getFragment(), operation2.getFragment(), z, lastInViews, false);
                        }
                    });
                    sharedElementFirstOutViews4.addAll(firstOutViews.values());
                    if (exitingNames.isEmpty()) {
                        exitingNames2 = exitingNames;
                    } else {
                        exitingNames2 = exitingNames;
                        String epicenterViewName = exitingNames2.get(0);
                        View firstOutEpicenterView5 = firstOutViews.get(epicenterViewName);
                        transitionImpl.setEpicenter(sharedElementTransition2, firstOutEpicenterView5);
                        firstOutEpicenterView4 = firstOutEpicenterView5;
                    }
                    sharedElementLastInViews3.addAll(lastInViews.values());
                    if (enteringNames.isEmpty()) {
                        lastInEpicenterRect4 = lastInEpicenterRect3;
                    } else {
                        String epicenterViewName2 = enteringNames.get(0);
                        final View lastInEpicenterView = lastInViews.get(epicenterViewName2);
                        if (lastInEpicenterView != null) {
                            hasLastInEpicenter = true;
                            final FragmentTransitionImpl impl = transitionImpl;
                            lastInEpicenterRect4 = lastInEpicenterRect3;
                            OneShotPreDrawListener.add(getContainer(), new Runnable() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.7
                                @Override // java.lang.Runnable
                                public void run() {
                                    impl.getBoundsOnScreen(lastInEpicenterView, lastInEpicenterRect4);
                                }
                            });
                        } else {
                            lastInEpicenterRect4 = lastInEpicenterRect3;
                        }
                    }
                    transitionImpl.setSharedElementTargets(sharedElementTransition2, nonExistentView4, sharedElementFirstOutViews4);
                    sharedElementFirstOutViews3 = sharedElementFirstOutViews4;
                    Rect rect = lastInEpicenterRect4;
                    nonExistentView2 = nonExistentView4;
                    sharedElementNameMapping = sharedElementNameMapping2;
                    lastInEpicenterRect2 = rect;
                    transitionImpl.scheduleRemoveTargets(sharedElementTransition2, null, null, null, null, sharedElementTransition2, sharedElementLastInViews3);
                    sharedElementLastInViews2 = sharedElementLastInViews3;
                    startedTransitions3 = startedTransitions4;
                    startedTransitions3.put(operation2, true);
                    startedTransitions3.put(operation3, true);
                    sharedElementTransition = sharedElementTransition2;
                    firstOutEpicenterView3 = firstOutEpicenterView4;
                }
            }
            lastInEpicenterRect5 = lastInEpicenterRect2;
            sharedElementLastInViews3 = sharedElementLastInViews2;
            startedTransitions5 = startedTransitions3;
            sharedElementFirstOutViews4 = sharedElementFirstOutViews3;
            sharedElementNameMapping2 = sharedElementNameMapping;
            nonExistentView3 = nonExistentView2;
            z = isPop;
        }
        ArrayList<View> arrayList = sharedElementFirstOutViews4;
        Map<SpecialEffectsController.Operation, Boolean> startedTransitions6 = startedTransitions5;
        ArrayList<View> enteringViews2 = arrayList;
        View nonExistentView5 = nonExistentView3;
        View firstOutEpicenterView6 = firstOutEpicenterView3;
        Rect lastInEpicenterRect6 = lastInEpicenterRect5;
        ArrayMap<String, String> sharedElementNameMapping3 = sharedElementNameMapping2;
        ArrayList<View> sharedElementLastInViews4 = sharedElementLastInViews3;
        ArrayList<View> enteringViews3 = new ArrayList<>();
        Object mergedTransition2 = null;
        Object mergedNonOverlappingTransition3 = null;
        for (TransitionInfo transitionInfo4 : transitionInfos) {
            if (transitionInfo4.isVisibilityUnchanged()) {
                startedTransitions6.put(transitionInfo4.getOperation(), false);
                transitionInfo4.completeSpecialEffect();
            } else {
                Object transition2 = transitionImpl.cloneTransition(transitionInfo4.getTransition());
                SpecialEffectsController.Operation operation4 = transitionInfo4.getOperation();
                boolean involvedInSharedElementTransition = sharedElementTransition != null && (operation4 == operation2 || operation4 == operation3);
                if (transition2 == null) {
                    if (involvedInSharedElementTransition) {
                        enteringViews = enteringViews3;
                    } else {
                        enteringViews = enteringViews3;
                        startedTransitions6.put(operation4, false);
                        transitionInfo4.completeSpecialEffect();
                    }
                    nonExistentView = nonExistentView5;
                    sharedElementFirstOutViews = enteringViews2;
                    sharedElementLastInViews = sharedElementLastInViews4;
                    startedTransitions2 = startedTransitions6;
                    sharedElementFirstOutViews2 = enteringViews;
                    firstOutEpicenterView2 = firstOutEpicenterView6;
                    str3 = str;
                } else {
                    ArrayList<View> enteringViews4 = enteringViews3;
                    ArrayList<View> transitioningViews2 = new ArrayList<>();
                    Object mergedTransition3 = mergedTransition2;
                    captureTransitioningViews(transitioningViews2, operation4.getFragment().mView);
                    if (involvedInSharedElementTransition) {
                        if (operation4 == operation2) {
                            transitioningViews2.removeAll(enteringViews2);
                        } else {
                            transitioningViews2.removeAll(sharedElementLastInViews4);
                        }
                    }
                    if (transitioningViews2.isEmpty()) {
                        transitionImpl.addTarget(transition2, nonExistentView5);
                        sharedElementFirstOutViews = enteringViews2;
                        sharedElementFirstOutViews2 = enteringViews4;
                        str3 = str;
                        mergedTransition = mergedTransition3;
                        nonExistentView = nonExistentView5;
                        sharedElementLastInViews = sharedElementLastInViews4;
                        transitioningViews = transitioningViews2;
                        startedTransitions2 = startedTransitions6;
                        mergedNonOverlappingTransition2 = mergedNonOverlappingTransition3;
                        transition = transition2;
                        operation = operation4;
                        firstOutEpicenterView2 = firstOutEpicenterView6;
                    } else {
                        transitionImpl.addTargets(transition2, transitioningViews2);
                        ArrayList<View> sharedElementLastInViews5 = sharedElementLastInViews4;
                        transition = transition2;
                        sharedElementLastInViews = sharedElementLastInViews5;
                        transitioningViews = transitioningViews2;
                        sharedElementFirstOutViews = enteringViews2;
                        sharedElementFirstOutViews2 = enteringViews4;
                        str3 = str;
                        mergedTransition = mergedTransition3;
                        nonExistentView = nonExistentView5;
                        firstOutEpicenterView2 = firstOutEpicenterView6;
                        startedTransitions2 = startedTransitions6;
                        mergedNonOverlappingTransition2 = mergedNonOverlappingTransition3;
                        transitionImpl.scheduleRemoveTargets(transition, transition, transitioningViews, null, null, null, null);
                        if (operation4.getFinalState() == SpecialEffectsController.Operation.State.GONE) {
                            operation = operation4;
                            awaitingContainerChanges.remove(operation);
                            ArrayList<View> transitioningViewsToHide = new ArrayList<>(transitioningViews);
                            transitioningViewsToHide.remove(operation.getFragment().mView);
                            transitionImpl.scheduleHideFragmentView(transition, operation.getFragment().mView, transitioningViewsToHide);
                            OneShotPreDrawListener.add(getContainer(), new Runnable() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.8
                                @Override // java.lang.Runnable
                                public void run() {
                                    FragmentTransition.setViewVisibility(transitioningViews, 4);
                                }
                            });
                        } else {
                            operation = operation4;
                        }
                    }
                    if (operation.getFinalState() == SpecialEffectsController.Operation.State.VISIBLE) {
                        sharedElementFirstOutViews2.addAll(transitioningViews);
                        if (hasLastInEpicenter) {
                            transitionImpl.setEpicenter(transition, lastInEpicenterRect6);
                        }
                    } else {
                        transitionImpl.setEpicenter(transition, firstOutEpicenterView2);
                    }
                    startedTransitions2.put(operation, true);
                    if (transitionInfo4.isOverlapAllowed()) {
                        mergedNonOverlappingTransition3 = mergedNonOverlappingTransition2;
                        mergedTransition2 = transitionImpl.mergeTransitionsTogether(mergedTransition, transition, null);
                    } else {
                        mergedNonOverlappingTransition3 = transitionImpl.mergeTransitionsTogether(mergedNonOverlappingTransition2, transition, null);
                        mergedTransition2 = mergedTransition;
                    }
                }
                operation3 = lastIn;
                firstOutEpicenterView6 = firstOutEpicenterView2;
                startedTransitions6 = startedTransitions2;
                enteringViews3 = sharedElementFirstOutViews2;
                str = str3;
                enteringViews2 = sharedElementFirstOutViews;
                sharedElementLastInViews4 = sharedElementLastInViews;
                nonExistentView5 = nonExistentView;
                operation2 = firstOut;
            }
        }
        ArrayList<View> sharedElementFirstOutViews5 = enteringViews2;
        ArrayList<View> sharedElementLastInViews6 = sharedElementLastInViews4;
        ArrayList<View> sharedElementFirstOutViews6 = enteringViews3;
        Map<SpecialEffectsController.Operation, Boolean> startedTransitions7 = startedTransitions6;
        Object mergedNonOverlappingTransition4 = mergedNonOverlappingTransition3;
        String str4 = str;
        View firstOutEpicenterView7 = firstOutEpicenterView6;
        Object mergedTransition4 = transitionImpl.mergeTransitionsInSequence(mergedTransition2, mergedNonOverlappingTransition4, sharedElementTransition);
        if (mergedTransition4 == null) {
            return startedTransitions7;
        }
        for (final TransitionInfo transitionInfo5 : transitionInfos) {
            if (!transitionInfo5.isVisibilityUnchanged()) {
                Object transition3 = transitionInfo5.getTransition();
                final SpecialEffectsController.Operation operation5 = transitionInfo5.getOperation();
                boolean involvedInSharedElementTransition2 = sharedElementTransition != null && (operation5 == firstOut || operation5 == lastIn);
                if (transition3 == null && !involvedInSharedElementTransition2) {
                    mergedNonOverlappingTransition = mergedNonOverlappingTransition4;
                    firstOutEpicenterView = firstOutEpicenterView7;
                    lastInEpicenterRect = lastInEpicenterRect6;
                    str2 = str4;
                    startedTransitions = startedTransitions7;
                } else if (!ViewCompat.isLaidOut(getContainer())) {
                    if (!FragmentManager.isLoggingEnabled(2)) {
                        mergedNonOverlappingTransition = mergedNonOverlappingTransition4;
                        firstOutEpicenterView = firstOutEpicenterView7;
                        str2 = str4;
                    } else {
                        mergedNonOverlappingTransition = mergedNonOverlappingTransition4;
                        firstOutEpicenterView = firstOutEpicenterView7;
                        str2 = str4;
                        Log.v(str2, "SpecialEffectsController: Container " + getContainer() + " has not been laid out. Completing operation " + operation5);
                    }
                    transitionInfo5.completeSpecialEffect();
                    startedTransitions = startedTransitions7;
                    lastInEpicenterRect = lastInEpicenterRect6;
                } else {
                    mergedNonOverlappingTransition = mergedNonOverlappingTransition4;
                    firstOutEpicenterView = firstOutEpicenterView7;
                    str2 = str4;
                    startedTransitions = startedTransitions7;
                    lastInEpicenterRect = lastInEpicenterRect6;
                    transitionImpl.setListenerForTransitionEnd(transitionInfo5.getOperation().getFragment(), mergedTransition4, transitionInfo5.getSignal(), new Runnable() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.9
                        @Override // java.lang.Runnable
                        public void run() {
                            transitionInfo5.completeSpecialEffect();
                            if (FragmentManager.isLoggingEnabled(2)) {
                                Log.v(FragmentManager.TAG, "Transition for operation " + operation5 + "has completed");
                            }
                        }
                    });
                }
                lastInEpicenterRect6 = lastInEpicenterRect;
                startedTransitions7 = startedTransitions;
                mergedNonOverlappingTransition4 = mergedNonOverlappingTransition;
                str4 = str2;
                firstOutEpicenterView7 = firstOutEpicenterView;
            }
        }
        String str5 = str4;
        Map<SpecialEffectsController.Operation, Boolean> startedTransitions8 = startedTransitions7;
        if (!ViewCompat.isLaidOut(getContainer())) {
            return startedTransitions8;
        }
        FragmentTransition.setViewVisibility(sharedElementFirstOutViews6, 4);
        ArrayList<String> inNames = transitionImpl.prepareSetNameOverridesReordered(sharedElementLastInViews6);
        if (FragmentManager.isLoggingEnabled(2)) {
            Log.v(str5, ">>>>> Beginning transition <<<<<");
            Log.v(str5, ">>>>> SharedElementFirstOutViews <<<<<");
            Iterator<View> it5 = sharedElementFirstOutViews5.iterator();
            while (it5.hasNext()) {
                View view3 = it5.next();
                Log.v(str5, "View: " + view3 + " Name: " + ViewCompat.getTransitionName(view3));
            }
            Log.v(str5, ">>>>> SharedElementLastInViews <<<<<");
            Iterator<View> it6 = sharedElementLastInViews6.iterator();
            while (it6.hasNext()) {
                View view4 = it6.next();
                Log.v(str5, "View: " + view4 + " Name: " + ViewCompat.getTransitionName(view4));
            }
        }
        transitionImpl.beginDelayedTransition(getContainer(), mergedTransition4);
        transitionImpl.setNameOverridesReordered(getContainer(), sharedElementFirstOutViews5, sharedElementLastInViews6, inNames, sharedElementNameMapping3);
        FragmentTransition.setViewVisibility(sharedElementFirstOutViews6, 0);
        transitionImpl.swapSharedElementTargets(sharedElementTransition, sharedElementFirstOutViews5, sharedElementLastInViews6);
        return startedTransitions8;
    }

    void retainMatchingViews(ArrayMap<String, View> sharedElementViews, Collection<String> transitionNames) {
        Iterator<Map.Entry<String, View>> iterator = sharedElementViews.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, View> entry = iterator.next();
            if (!transitionNames.contains(ViewCompat.getTransitionName(entry.getValue()))) {
                iterator.remove();
            }
        }
    }

    void captureTransitioningViews(ArrayList<View> transitioningViews, View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            if (ViewGroupCompat.isTransitionGroup(viewGroup)) {
                if (!transitioningViews.contains(view)) {
                    transitioningViews.add(viewGroup);
                    return;
                }
                return;
            }
            int count = viewGroup.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = viewGroup.getChildAt(i);
                if (child.getVisibility() == 0) {
                    captureTransitioningViews(transitioningViews, child);
                }
            }
            return;
        }
        if (!transitioningViews.contains(view)) {
            transitioningViews.add(view);
        }
    }

    void findNamedViews(Map<String, View> namedViews, View view) {
        String transitionName = ViewCompat.getTransitionName(view);
        if (transitionName != null) {
            namedViews.put(transitionName, view);
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int count = viewGroup.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = viewGroup.getChildAt(i);
                if (child.getVisibility() == 0) {
                    findNamedViews(namedViews, child);
                }
            }
        }
    }

    void applyContainerChanges(SpecialEffectsController.Operation operation) {
        View view = operation.getFragment().mView;
        operation.getFinalState().applyState(view);
    }

    private static class SpecialEffectsInfo {
        private final SpecialEffectsController.Operation mOperation;
        private final CancellationSignal mSignal;

        SpecialEffectsInfo(SpecialEffectsController.Operation operation, CancellationSignal signal) {
            this.mOperation = operation;
            this.mSignal = signal;
        }

        SpecialEffectsController.Operation getOperation() {
            return this.mOperation;
        }

        CancellationSignal getSignal() {
            return this.mSignal;
        }

        boolean isVisibilityUnchanged() {
            SpecialEffectsController.Operation.State currentState = SpecialEffectsController.Operation.State.from(this.mOperation.getFragment().mView);
            SpecialEffectsController.Operation.State finalState = this.mOperation.getFinalState();
            return currentState == finalState || !(currentState == SpecialEffectsController.Operation.State.VISIBLE || finalState == SpecialEffectsController.Operation.State.VISIBLE);
        }

        void completeSpecialEffect() {
            this.mOperation.completeSpecialEffect(this.mSignal);
        }
    }

    private static class AnimationInfo extends SpecialEffectsInfo {
        private FragmentAnim.AnimationOrAnimator mAnimation;
        private boolean mIsPop;
        private boolean mLoadedAnim;

        AnimationInfo(SpecialEffectsController.Operation operation, CancellationSignal signal, boolean isPop) {
            super(operation, signal);
            this.mLoadedAnim = false;
            this.mIsPop = isPop;
        }

        FragmentAnim.AnimationOrAnimator getAnimation(Context context) {
            if (this.mLoadedAnim) {
                return this.mAnimation;
            }
            this.mAnimation = FragmentAnim.loadAnimation(context, getOperation().getFragment(), getOperation().getFinalState() == SpecialEffectsController.Operation.State.VISIBLE, this.mIsPop);
            this.mLoadedAnim = true;
            return this.mAnimation;
        }
    }

    private static class TransitionInfo extends SpecialEffectsInfo {
        private final boolean mOverlapAllowed;
        private final Object mSharedElementTransition;
        private final Object mTransition;

        TransitionInfo(SpecialEffectsController.Operation operation, CancellationSignal signal, boolean isPop, boolean providesSharedElementTransition) {
            Object exitTransition;
            Object enterTransition;
            boolean allowEnterTransitionOverlap;
            super(operation, signal);
            if (operation.getFinalState() == SpecialEffectsController.Operation.State.VISIBLE) {
                if (isPop) {
                    enterTransition = operation.getFragment().getReenterTransition();
                } else {
                    enterTransition = operation.getFragment().getEnterTransition();
                }
                this.mTransition = enterTransition;
                if (isPop) {
                    allowEnterTransitionOverlap = operation.getFragment().getAllowReturnTransitionOverlap();
                } else {
                    allowEnterTransitionOverlap = operation.getFragment().getAllowEnterTransitionOverlap();
                }
                this.mOverlapAllowed = allowEnterTransitionOverlap;
            } else {
                if (isPop) {
                    exitTransition = operation.getFragment().getReturnTransition();
                } else {
                    exitTransition = operation.getFragment().getExitTransition();
                }
                this.mTransition = exitTransition;
                this.mOverlapAllowed = true;
            }
            if (providesSharedElementTransition) {
                if (isPop) {
                    this.mSharedElementTransition = operation.getFragment().getSharedElementReturnTransition();
                    return;
                } else {
                    this.mSharedElementTransition = operation.getFragment().getSharedElementEnterTransition();
                    return;
                }
            }
            this.mSharedElementTransition = null;
        }

        Object getTransition() {
            return this.mTransition;
        }

        boolean isOverlapAllowed() {
            return this.mOverlapAllowed;
        }

        public boolean hasSharedElementTransition() {
            return this.mSharedElementTransition != null;
        }

        public Object getSharedElementTransition() {
            return this.mSharedElementTransition;
        }

        FragmentTransitionImpl getHandlingImpl() {
            FragmentTransitionImpl transitionImpl = getHandlingImpl(this.mTransition);
            FragmentTransitionImpl sharedElementTransitionImpl = getHandlingImpl(this.mSharedElementTransition);
            if (transitionImpl == null || sharedElementTransitionImpl == null || transitionImpl == sharedElementTransitionImpl) {
                return transitionImpl != null ? transitionImpl : sharedElementTransitionImpl;
            }
            throw new IllegalArgumentException("Mixing framework transitions and AndroidX transitions is not allowed. Fragment " + getOperation().getFragment() + " returned Transition " + this.mTransition + " which uses a different Transition  type than its shared element transition " + this.mSharedElementTransition);
        }

        private FragmentTransitionImpl getHandlingImpl(Object transition) {
            if (transition == null) {
                return null;
            }
            if (FragmentTransition.PLATFORM_IMPL != null && FragmentTransition.PLATFORM_IMPL.canHandle(transition)) {
                return FragmentTransition.PLATFORM_IMPL;
            }
            if (FragmentTransition.SUPPORT_IMPL != null && FragmentTransition.SUPPORT_IMPL.canHandle(transition)) {
                return FragmentTransition.SUPPORT_IMPL;
            }
            throw new IllegalArgumentException("Transition " + transition + " for fragment " + getOperation().getFragment() + " is not a valid framework Transition or AndroidX Transition");
        }
    }
}
