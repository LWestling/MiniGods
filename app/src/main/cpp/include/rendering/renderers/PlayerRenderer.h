//
// Created by LukasW on 2018-09-13.
//

#ifndef MINIGODS_PLAYERRENDERER_H
#define MINIGODS_PLAYERRENDERER_H

#include <assimp/scene.h>
#include "RendererInterface.h"

class PlayerRenderer : public RendererInterface {
private:
    aiScene* player;
public:
    void onCreate();
    void onRender();
    void onUpdate();
};


#endif //MINIGODS_PLAYERRENDERER_H
