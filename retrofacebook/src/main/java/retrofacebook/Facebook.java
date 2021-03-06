/*
 * Copyright (C) 2015 8tory, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package retrofacebook;

import rx.Observable;
import rx.Subscriber;

import rx.functions.*;

import com.facebook.*;
import com.facebook.login.*;

import android.content.Intent;
import android.app.Activity;

import java.util.Collection;
import java.util.List;
import java.util.Arrays;

@RetroFacebook
public abstract class Facebook {
    @RetroFacebook.GET("/{post-id}")
    public abstract Observable<Post> getPost(@RetroFacebook.Path("post-id") String postId);

    // TODO @RetroFacebook.GET("/{userId}/photos?type=uploaded")
    @RetroFacebook.GET("/{user-id}/photos")
    public abstract Observable<Photo> getPhotos(@RetroFacebook.Path("user-id") String userId);

    public Observable<Photo> getPhotos() {
        return getPhotos("me");
    }

    @RetroFacebook.GET("/{user-id}/feed")
    public abstract Observable<Post> getPosts(@RetroFacebook.Path("user-id") String userId);

    public Observable<Post> getPosts() {
        return getPosts("me");
    }

    @RetroFacebook.GET("/{user-id}/friends")
    public abstract Observable<User> getFriends(@RetroFacebook.Path("user-id") String userId);

    public Observable<User> getFriends() {
        return getFriends("me");
    }

    public static Facebook create() {
        return new RetroFacebook_Facebook();
    }

    CallbackManager callbackManager;
    Activity activity;

    public Facebook initialize(Activity activity) {
        this.activity = activity;

        FacebookSdk.sdkInitialize(activity.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        return this;
    }

    public static Facebook create(Activity activity) {
        return create().initialize(activity);
    }

    public Observable<LoginResult> logIn() {
        return logInWithReadPermissions(Arrays.asList("public_profile", "user_friends", "user_photos", "user_posts"));
    }

    public Observable<LoginResult> logInWithReadPermissions(final Collection<String> permissions) {
        return Observable.create(new Observable.OnSubscribe<LoginResult>() {
            @Override public void call(final Subscriber<? super LoginResult> sub) {
                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        sub.onNext(loginResult);
                        sub.onCompleted();
                    }

                    @Override
                    public void onCancel() {
                        sub.onCompleted();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        sub.onError(error);
                    }
                });
                LoginManager.getInstance().logInWithReadPermissions(activity, permissions);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
