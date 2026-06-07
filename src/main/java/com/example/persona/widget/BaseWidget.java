package com.example.persona.widget;

import com.example.persona.profile.dto.response.UserProfileResponse;

public class BaseWidget {
    private UserProfileResponse profile;

    public BaseWidget(UserProfileResponse profile) {
        this.profile = profile;
    }

    public void render() {
        // Render the widget
    }
}
