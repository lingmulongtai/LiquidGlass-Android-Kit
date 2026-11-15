package io.github.lingmulongtai.liquidglass

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/** Keep the native text editor and accessibility semantics inside a Glisse-style glass input. */
@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = false
) {
    val dark = LocalGlassDark.current
    val colors = MaterialTheme.colorScheme
    val nested = LocalGlassPanelDepth.current > 0
    GlassSurface(
        backdrop = if (nested) null else LocalGlassBackdrop.current,
        shape = RoundedCornerShape(20.dp),
        tint = if (dark) Color(0xFF282828).copy(alpha = 0.62f) else Color.White.copy(alpha = 0.60f),
        fallback = if (nested) {
            if (dark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
        } else colors.surfaceContainerHigh,
        rim = if (dark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f),
        modifier = modifier, blurRadius = 3.dp, refraction = 0f,
        elevation = if (dark) 0.dp else 1.dp
    ) {
        OutlinedTextField(value, onValueChange, Modifier.fillMaxWidth(), enabled = enabled, label = label,
            placeholder = placeholder, leadingIcon = leadingIcon, trailingIcon = trailingIcon,
            supportingText = supportingText, isError = isError, visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions, singleLine = singleLine,
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isError) colors.error else colors.primary.copy(alpha = 0.65f),
                unfocusedBorderColor = Color.Transparent, disabledBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent, errorContainerColor = Color.Transparent
            ))
    }
}
