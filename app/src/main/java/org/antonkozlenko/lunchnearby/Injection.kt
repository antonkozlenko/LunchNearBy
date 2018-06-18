/*
 * Copyright (C) 2018 The Android Open Source Project
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

package org.antonkozlenko.lunchnearby

import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import org.antonkozlenko.lunchnearby.api.GooglePlacesService
import org.antonkozlenko.lunchnearby.data.GooglePlacesRepository
import org.antonkozlenko.lunchnearby.ui.AppViewModelFactory

/**
 * Class that handles object creation.
 * Like this, objects can be passed as parameters in the constructors and then replaced for
 * testing, where needed.
 */
object Injection {

    fun provideGooglePlacesRepository(context: Context): GooglePlacesRepository {
        return GooglePlacesRepository(GooglePlacesService.create())
    }

    fun provideAppViewModelFactory(context: Context): ViewModelProvider.Factory {
        return AppViewModelFactory(provideGooglePlacesRepository(context))
    }

}