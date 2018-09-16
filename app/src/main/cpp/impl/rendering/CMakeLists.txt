target_sources(game_engine PRIVATE
                ${CMAKE_CURRENT_LIST_DIR}/OGLManager.cpp)

add_subdirectory(${CMAKE_CURRENT_LIST_DIR}/model/)
add_subdirectory(${CMAKE_CURRENT_LIST_DIR}/renderers/)
