//
// Created by LukasW on 2018-09-13.
//

#ifndef MINIGODS_RENDERERINTERFACE_H
#define MINIGODS_RENDERERINTERFACE_H


class RendererInterface {
public:
    virtual void onCreate() = 0;
    virtual void onRender() = 0;
    virtual void onUpdate() = 0;
};


#endif //MINIGODS_RENDERERINTERFACE_H
