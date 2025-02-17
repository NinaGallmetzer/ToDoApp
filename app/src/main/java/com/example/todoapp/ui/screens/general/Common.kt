package com.example.todoapp.ui.screens.general

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.todoapp.R

@Composable
fun CommonAppBar(
    title: String = stringResource(R.string.add_note),
    navController: NavController
) {
    Box(modifier =  Modifier
        .fillMaxWidth(),
    ) {
        Row(modifier = Modifier
            .background(MaterialTheme.colors.background)
            .padding(horizontal = 10.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Return",
                tint = MaterialTheme.colors.onBackground,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .clickable(onClick = {
                        navController.popBackStack()
                    }),
            )
            Text(
                modifier = Modifier
                    .weight(0.5f)
                    .align(Alignment.CenterVertically),
                textAlign = TextAlign.Center,
                text = title,
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.onBackground
            )
        }
    }
}

@Composable
fun MainButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector? = null,
    backgroundColor: Color = MaterialTheme.colors.background,
    contentColor: Color = MaterialTheme.colors.onBackground,

    shape: RoundedCornerShape = RoundedCornerShape(24),
    padding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
    textStyle: TextStyle = MaterialTheme.typography.button.copy(fontWeight = FontWeight.Bold),
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = backgroundColor,
            contentColor = contentColor
        ),
        contentPadding = padding,
        elevation = ButtonDefaults.elevation(defaultElevation = 8.dp, pressedElevation = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = " $text",
                style = textStyle,
                color = contentColor
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

fun showDialog(
    context: Context,
    title: String = "",
    message: String = "",
    positive: String = "",
    negative: String = "",
    onPositiveClick: () -> Unit,
    onNegativeClick: () -> Unit
) {
    val dialogBuilder = android.app.AlertDialog.Builder(context)
    dialogBuilder.setTitle(title)
    dialogBuilder.setMessage(message)
    dialogBuilder.setPositiveButton(positive) { _, _ ->
        onPositiveClick()
    }
    dialogBuilder.setNegativeButton(negative) { _, _ ->
        onNegativeClick()
    }

    dialogBuilder.create().show()
}

@Composable
fun CustomOutlinedTextField(
    new: Boolean,
    startValue: Any?,
    modifier: Modifier,
    textValue: String?,
    textLabel: String,
    keyboardType: KeyboardType,
    onValueChange: (String) -> Unit
) {
    var value by remember { mutableStateOf<Any?>(null) }

    LaunchedEffect(startValue) {
        value = startValue
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart,
    ) {
        OutlinedTextField(
            value =
            if (new) {
                if (value == null) "" else (textValue ?: "")
            } else {
                textValue ?: ""
            },
            onValueChange = {
                value = it
                onValueChange(it)
            },
            label = {
                Text(text = textLabel)
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            textStyle = TextStyle(fontSize = 18.sp),
            shape = MaterialTheme.shapes.medium,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colors.background,
                unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                focusedLabelColor = MaterialTheme.colors.background,
                cursorColor = MaterialTheme.colors.background
            )
        )
    }
}

@Composable
fun CommonAddFAB(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FloatingActionButton(
        modifier = modifier,
        onClick = { onClick() },
        shape = CircleShape,
    ) {
        Icon(Icons.Filled.Add, "Floating action button.")
    }
}
