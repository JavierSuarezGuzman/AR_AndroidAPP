/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.javiersg.proyectoandroid.hellogeospatial

import android.opengl.Matrix
import android.util.Log
import android.widget.TextView

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

import com.google.ar.core.Anchor
import com.google.ar.core.Earth
import com.google.ar.core.GeospatialPose
import com.google.ar.core.TrackingState

import com.javiersg.proyectoandroid.common.helpers.DisplayRotationHelper
import com.javiersg.proyectoandroid.common.helpers.TrackingStateHelper
import com.javiersg.proyectoandroid.common.samplerender.Framebuffer
import com.javiersg.proyectoandroid.common.samplerender.Mesh
import com.javiersg.proyectoandroid.common.samplerender.SampleRender
import com.javiersg.proyectoandroid.common.samplerender.Shader
import com.javiersg.proyectoandroid.common.samplerender.Texture
import com.javiersg.proyectoandroid.common.samplerender.arcore.BackgroundRenderer

import com.google.ar.core.exceptions.CameraNotAvailableException
import com.javiersg.proyectoandroid.hellogeospatial.helpers.ARCoreSessionLifecycleHelper

import java.io.IOException

class HelloGeoRenderer(val activity: HelloGeoActivity) :
  SampleRender.Renderer, DefaultLifecycleObserver {
  //<editor-fold desc="Inicialización de ARCore" defaultstate="collapsed">
  companion object {
    val TAG = "HelloGeoRenderer"

    private val Z_NEAR = 0.1f
    private val Z_FAR = 1000f
  }

  lateinit var arCoreSessionHelper: ARCoreSessionLifecycleHelper

  lateinit var backgroundRenderer: BackgroundRenderer
  lateinit var virtualSceneFramebuffer: Framebuffer
  var hasSetTextureNames = false

  // Virtual object (ARCore pawn)
  lateinit var virtualObjectMesh: Mesh
  lateinit var virtualObjectShader: Shader
  lateinit var virtualObjectTexture: Texture

  // Temporary matrix allocated here to reduce number of allocations for each frame.
  val modelMatrix = FloatArray(16)
  val viewMatrix = FloatArray(16)
  val projectionMatrix = FloatArray(16)
  val modelViewMatrix = FloatArray(16) // view x model

  val modelViewProjectionMatrix = FloatArray(16) // projection x view x model

  val session
    get() = activity.arCoreSessionHelper.session

  val displayRotationHelper = DisplayRotationHelper(activity)
  val trackingStateHelper = TrackingStateHelper(activity)

  override fun onResume(owner: LifecycleOwner) {
    displayRotationHelper.onResume()
    hasSetTextureNames = false
  }

  override fun onPause(owner: LifecycleOwner) {
    displayRotationHelper.onPause()
  }

  override fun onSurfaceCreated(render: SampleRender) {
    // Prepare the rendering objects.
    // This involves reading shaders and 3D model files, so may throw an IOException.
    try {
      backgroundRenderer = BackgroundRenderer(render)
      virtualSceneFramebuffer = Framebuffer(render, /*width=*/ 1, /*height=*/ 1)

      // Virtual object to render (Geospatial Marker)
      virtualObjectTexture =
        Texture.createFromAsset(
          render,
          "models/spatial_marker_baked.png",
          Texture.WrapMode.CLAMP_TO_EDGE,
          Texture.ColorFormat.SRGB
        )

      virtualObjectMesh = Mesh.createFromAsset(render, "models/geospatial_marker.obj");
      virtualObjectShader =
        Shader.createFromAssets(
          render,
          "shaders/ar_unlit_object.vert",
          "shaders/ar_unlit_object.frag",
          /*defines=*/ null)
          .setTexture("u_Texture", virtualObjectTexture)

      backgroundRenderer.setUseDepthVisualization(render, false)
      backgroundRenderer.setUseOcclusion(render, false)
    } catch (e: IOException) {
      Log.e(TAG, "Failed to read a required asset file", e)
      showError("Failed to read a required asset file: $e")
    }
  }

  override fun onSurfaceChanged(render: SampleRender, width: Int, height: Int) {
    displayRotationHelper.onSurfaceChanged(width, height)
    virtualSceneFramebuffer.resize(width, height)
  }
  //</editor-fold>

  override fun onDrawFrame(render: SampleRender) {
    val session = session ?: return

    //<editor-fold desc="ARCore frame boilerplate" defaultstate="collapsed">
    // Texture names should only be set once on a GL thread unless they change. This is done during
    // onDrawFrame rather than onSurfaceCreated since the session is not guaranteed to have been
    // initialized during the execution of onSurfaceCreated.
    if (!hasSetTextureNames) {
      session.setCameraTextureNames(intArrayOf(backgroundRenderer.cameraColorTexture.textureId))
      hasSetTextureNames = true
    }

    // -- Update per-frame state

    // Notify ARCore session that the view size changed so that the perspective matrix and
    // the video background can be properly adjusted.
    displayRotationHelper.updateSessionIfNeeded(session)

    // Obtain the current frame from ARSession. When the configuration is set to
    // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
    // camera framerate.
    val frame =
      try {
        session.update()
      } catch (e: CameraNotAvailableException) {
        Log.e(TAG, "Camera not available during onDrawFrame", e)
        showError("Camera not available. Try restarting the app.")
        return
      }

    val camera = frame.camera

    // BackgroundRenderer.updateDisplayGeometry must be called every frame to update the coordinates
    // used to draw the background camera image.
    backgroundRenderer.updateDisplayGeometry(frame)

    // Keep the screen unlocked while tracking, but allow it to lock when tracking stops.
    trackingStateHelper.updateKeepScreenOnFlag(camera.trackingState)

    // -- Draw background
    if (frame.timestamp != 0L) {
      // Suppress rendering if the camera did not produce the first frame yet. This is to avoid
      // drawing possible leftover data from previous sessions if the texture is reused.
      backgroundRenderer.drawBackground(render)
    }

    // If not tracking, don't draw 3D objects.
    if (camera.trackingState == TrackingState.PAUSED) {
      return
    }

    // Get projection matrix.
    camera.getProjectionMatrix(projectionMatrix, 0, Z_NEAR, Z_FAR)

    // Get camera matrix and draw.
    camera.getViewMatrix(viewMatrix, 0)

    render.clear(virtualSceneFramebuffer, 0f, 0f, 0f, 0f)
    //</editor-fold>

    // DONE: Obtener información geoespacial y mostrar flecha en el mapa.
    val earth = session.earth
    /* v Del objeto 'earth' su TrackingState es TRACKING, esto significa que la posición es conocida. */
    if (earth?.trackingState == TrackingState.TRACKING
        && earth.earthState == Earth.EarthState.ENABLED){ //https://developers.google.com/ar/reference/java/com/google/ar/core/Earth.EarthState
      // DONE: El objeto earth se debe usar aquí:
      //aquí se le solicita al ARCore que entregue los datos "geoespaciales"
      val cameraGeoSpatialPose = earth.cameraGeospatialPose
      /* ^ Esto entrega la "GeospatialPose" que contiene la siguiente información:
    -Location, expressed in latitude and longitude. An estimate of the location accuracy is also supplied.
    -Elevation, and an estimate of elevation accuracy.
    -Heading, an approximation of the direction the device is facing, and an estimate of the accuracy of the heading.*/
      // Display positioning information on the map
      activity.view.mapView?.updateMapPosition(
        latitude = cameraGeoSpatialPose.latitude,
        longitude = cameraGeoSpatialPose.longitude,
        heading = cameraGeoSpatialPose.heading
      )
    }
    /* Mostrar datos en pantalla
    // v Esta línea debería poder mostrar en pantalla el estado de información de la ubicación
    //activity.view.updateStatusText(earth, earth.cameraGeospatialPose)
    // ^ Pero falla y aún no entiendo por qué*/

    // Mostrar un "ancla" o anchor si existe.
    earthAnchor?.let {
      render.renderCompassAtAnchor(it)
    }

    // Compose the virtual scene with the background.
    backgroundRenderer.drawVirtualScene(render, virtualSceneFramebuffer, Z_NEAR, Z_FAR)
  }

  var earthAnchor: Anchor? = null

  fun onMapClick(latLng: LatLng) {
    // DONE: Ubicar un anchor/ancla en la posición pinchada.
    val earth = session?.earth ?: return
    if (earth.trackingState != TrackingState.TRACKING) {
      return
    }
    earthAnchor?.detach()
    // Se posiciona la ancla en una altitud menor para poder verla sin problemas.
    val altitude = earth.cameraGeospatialPose.altitude - 22.3
    // The rotation quaternion of the anchor in the East-Up-South (EUS) coordinate system.
    val qx = 0f
    val qy = 0f
    val qz = 0f
    val qw = 1f
    earthAnchor = earth.createAnchor(latLng.latitude, latLng.longitude, altitude, qx, qy, qz, qw)

    // Mostrar la marca en el mapa
    activity.view.mapView?.earthMarker?.apply {
      position = latLng
      isVisible = true
    }
  }

  private fun SampleRender.renderCompassAtAnchor(anchor: Anchor) {
    // Renderizar el .obj en pantalla cuando este es actualizado en la función anterior
    // Get the current pose of the Anchor in world space. The Anchor pose is updated
    // during calls to session.update() as ARCore refines its estimate of the world.
    anchor.pose.toMatrix(modelMatrix, 0)

    // Calculate model/view/projection matrices
    Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0)
    Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0)

    // Update shader properties and draw
    virtualObjectShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix)
    draw(virtualObjectMesh, virtualObjectShader, virtualSceneFramebuffer)
  }

  private fun showError(errorMessage: String) =
    activity.view.snackbarHelper.showError(activity, errorMessage)
}
