/*
 * OneBox is a free AI agent and toolbox for Android
 * This file is part of OneBox, a derivative work based on
 * ImageToolbox (https://github.com/T8RIN/ImageToolbox) by T8RIN.
 *
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file has been modified from the original ImageToolbox source.
 *
 * Origin: ImageToolbox derivative, modified
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.core.ui.utils.animation

import androidx.compose.animation.core.CubicBezierEasing

val FancyTransitionEasing = CubicBezierEasing(0.48f, 0.19f, 0.05f, 1.03f)

val AlphaEasing = CubicBezierEasing(0.4f, 0.4f, 0.17f, 0.9f)

val PointToPointEasing = CubicBezierEasing(0.55f, 0.55f, 0f, 1f)

val FastInvokeEasing = CubicBezierEasing(0f, 0f, 0f, 1f)

val OverslideEasing = CubicBezierEasing(0.5f, 0.5f, 1.0f, 0.25f)

val RotationEasing = CubicBezierEasing(0.46f, 0.03f, 0.52f, 0.96f)