package com.wezom.kiviremoteserver;

interface IPlayerListener {
//    int STATE_PLAY = 1;
//        int STATE_PAUSE = 2;
//        int STATE_CLOSE = 3;
//        int STATE_ADVERTISING = 4;
        void launchPlayer(int contentId, int parentContentId, String title, String description, String imageUrl, int duration);
        void changeState(int newState);
        void seekTo(int progressPercent);
}
