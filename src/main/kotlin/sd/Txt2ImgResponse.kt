package sd

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Txt2ImgResponse(
    @SerialName("images")
    var images: ArrayList<String> = arrayListOf(),
    @SerialName("parameters")
    var parameters: Parameters? = Parameters(),
    @SerialName("info")
    var info: String? = null

) {
    @Serializable
    data class Parameters(
        @SerialName("enable_hr")
        var enableHr: Boolean? = null,
        @SerialName("denoising_strength")
        var denoisingStrength: Int? = null,
        @SerialName("firstphase_width")
        var firstphaseWidth: Int? = null,
        @SerialName("firstphase_height")
        var firstphaseHeight: Int? = null,
        @SerialName("hr_scale")
        var hrScale: Float? = null,
        @SerialName("hr_upscaler")
        var hrUpscaler: String? = null,
        @SerialName("hr_second_pass_steps")
        var hrSecondPassSteps: Int? = null,
        @SerialName("hr_resize_x")
        var hrResizeX: Int? = null,
        @SerialName("hr_resize_y")
        var hrResizeY: Int? = null,
        @SerialName("prompt")
        var prompt: String? = null,
        @SerialName("styles")
        var styles: String? = null,
        @SerialName("seed")
        var seed: Long? = null,
        @SerialName("subseed")
        var subseed: Int? = null,
        @SerialName("subseed_strength")
        var subseedStrength: Int? = null,
        @SerialName("seed_resize_from_h")
        var seedResizeFromH: Int? = null,
        @SerialName("seed_resize_from_w")
        var seedResizeFromW: Int? = null,
        @SerialName("sampler_name")
        var samplerName: String? = null,
        @SerialName("batch_size")
        var batchSize: Int? = null,
        @SerialName("n_iter")
        var nIter: Int? = null,
        @SerialName("steps")
        var steps: Int? = null,
        @SerialName("cfg_scale")
        var cfgScale: Float? = null,
        @SerialName("width")
        var width: Int? = null,
        @SerialName("height")
        var height: Int? = null,
        @SerialName("restore_faces")
        var restoreFaces: Boolean? = null,
        @SerialName("tiling")
        var tiling: Boolean? = null,
        @SerialName("do_not_save_samples")
        var doNotSaveSamples: Boolean? = null,
        @SerialName("do_not_save_grid")
        var doNotSaveGrid: Boolean? = null,
        @SerialName("negative_prompt")
        var negativePrompt: String? = null,
        @SerialName("eta")
        var eta: String? = null,
        @SerialName("s_churn")
        var sChurn: Float? = null,
        @SerialName("s_tmax")
        var sTmax: String? = null,
        @SerialName("s_tmin")
        var sTmin: Float? = null,
        @SerialName("s_noise")
        var sNoise: Float? = null,
        @SerialName("override_settings")
        var overrideSettings: String? = null,
        @SerialName("override_settings_restore_afterwards")
        var overrideSettingsRestoreAfterwards: Boolean? = null,
        @SerialName("script_args")
        var scriptArgs: ArrayList<String> = arrayListOf(),
        @SerialName("sampler_index")
        var samplerIndex: String? = null,
        @SerialName("script_name")
        var scriptName: String? = null,
        @SerialName("send_images")
        var sendImages: Boolean? = null,
        @SerialName("save_images")
        var saveImages: Boolean? = null,
    )
}