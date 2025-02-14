package com.example.todoapp.ui.screens.general

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.todoapp.R
import com.example.todoapp.data.utils.ExportDbUtil
import com.example.todoapp.ui.viewmodels.DataScreenViewModel
import com.example.todoapp.ui.viewmodels.InjectorUtils


@Composable
fun DataScreen(
    navController: NavController,
    exportDbUtil: ExportDbUtil
) {
    val title = stringResource(R.string.import_export_data)
    val image = R.drawable.background_portraint

    val dataScreenViewModel: DataScreenViewModel = viewModel(factory = InjectorUtils.provideDataScreenViewModelFactory(exportDbUtil = exportDbUtil))

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("permissionManager", "permission granted")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .fillMaxWidth(),
            contentScale = ContentScale.FillBounds
        )
        CommonAppBar(title = title, navController = navController)

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MainButton(
                // TODO translate
                text = "Export as individual CSV-Files",
                icon = Icons.Default.Share,
            ) {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                dataScreenViewModel.downloadTables()
            }
        }
    }
}
