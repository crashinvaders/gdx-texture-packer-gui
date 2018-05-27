package com.crashinvaders.texturepackergui.views.seekbar;

public interface SeekBarModel {
    void bind(SeekBar seekBar);
    void onTextValueChanged();
    void onSliderValueChanged();
    String prepareTextValue();
    float prepareSliderValue();
}
