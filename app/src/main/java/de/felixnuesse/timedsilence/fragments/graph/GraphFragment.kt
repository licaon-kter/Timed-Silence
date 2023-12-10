package de.felixnuesse.timedsilence.fragments.graph

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.skydoves.balloon.*
import de.felixnuesse.timedsilence.R
import de.felixnuesse.timedsilence.databinding.FragmentGraphBinding
import de.felixnuesse.timedsilence.handler.PreferencesManager
import de.felixnuesse.timedsilence.handler.calculator.HeadsetHandler
import de.felixnuesse.timedsilence.handler.volume.VolumeState
import de.felixnuesse.timedsilence.handler.volume.VolumeState.Companion.TIME_SETTING_LOUD
import de.felixnuesse.timedsilence.handler.volume.VolumeState.Companion.TIME_SETTING_SILENT
import de.felixnuesse.timedsilence.handler.volume.VolumeState.Companion.TIME_SETTING_UNSET
import de.felixnuesse.timedsilence.handler.volume.VolumeState.Companion.TIME_SETTING_VIBRATE
import de.felixnuesse.timedsilence.util.SizeUtil
import de.felixnuesse.timedsilence.volumestate.StateGenerator
import java.util.concurrent.TimeUnit


class GraphFragment : Fragment() {

    private lateinit var viewObject: View

    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGraphBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewObject = view

        binding.imageviewLegendLoudHelp.setOnClickListener {
            handleTooltipRight(requireContext(), getString(R.string.volume_setting_loud_help), it)
        }
        binding.imageviewLegendSilentHelp.setOnClickListener {
            handleTooltipRight(requireContext(), getString(R.string.volume_setting_silent_help), it)
        }
        binding.imageviewLegendVibrateHelp.setOnClickListener {
            handleTooltipRight(requireContext(), getString(R.string.volume_setting_vibrate_help), it)
        }
        binding.imageviewLegendUnsetHelp.setOnClickListener {
            handleTooltipRight(requireContext(), getString(R.string.volume_setting_unset_help), it)
        }

        binding.imageviewHeadphonesConnected.setOnClickListener {
            var builder = getTooltip(requireContext(), getString(R.string.headphones_help))
            builder.setArrowPosition(0.75f)
            it.showAlignTop(builder.build())
        }

        if(!HeadsetHandler.headphonesConnected(view.context)){
            binding.imageviewHeadphonesConnected.visibility=View.INVISIBLE
            binding.textfieldHeadsetConnected.visibility=View.INVISIBLE
        }

        if(!PreferencesManager(view.context).checkIfHeadsetIsConnected()){
            binding.imageviewHeadphonesConnected.visibility=View.INVISIBLE
            binding.textfieldHeadsetConnected.visibility=View.INVISIBLE
        }
        Thread {
            buildGraph(view.context, binding.relLayout)
        }.start()



    }

    fun buildGraph(context:Context, relLayout: LinearLayout){

        setLegendColor(R.color.color_graph_unset, binding.imageViewLegendUnset)
        setLegendColor(R.color.color_graph_silent, binding.imageViewLegendSilent)
        setLegendColor(R.color.color_graph_vibrate, binding.imageViewLegendVibrate)
        setLegendColor(R.color.color_graph_loud, binding.imageViewLegendLoud)

        val volCalc = StateGenerator(requireContext())
        val list = volCalc.states()
        var isfirst = true

        val barElementList = arrayListOf<LinearLayout>()

        for (i in list.indices){
            val volumeState = list[i]

            val color = resources.getColor(getColorFromState(volumeState.state))
            var nextColor: Int? = null

            if(list.size-1>i){
                nextColor = resources.getColor(getColorFromState(list[i+1].state))
            }

            barElementList.add(createBarElem(context, volumeState, color, nextColor, isfirst))
            isfirst = false
        }

        requireActivity().runOnUiThread {
            barElementList.forEach {
                relLayout.addView(it)
            }
            binding.loadingColumn.visibility = View.GONE
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun createBarElem(context: Context, volumeState: VolumeState, color: Int, nextColor: Int?, isFirst: Boolean): LinearLayout {

        val minutes = TimeUnit.MILLISECONDS.toMinutes(volumeState.duration)
        var outerLayout = LinearLayout(context)
        val outerLayoutParams = LinearLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            minutes.toFloat()
        )

        outerLayout.layoutParams = outerLayoutParams

        //outerLayout.setBackgroundColor(color)
        val item = if(nextColor != null) {
            GraphItemView(context.applicationContext, color, isFirst, nextColor)
        } else {
            GraphItemView(context.applicationContext, color, isFirst)
        }

        item.setTooltip(volumeState.getReason())
        item.updateVisibility(volumeState.duration)


        var text_addition=""
        when (volumeState.state) {
            TIME_SETTING_SILENT -> text_addition = "Silent"
            TIME_SETTING_VIBRATE -> text_addition = "Vibrate"
            TIME_SETTING_LOUD -> text_addition = "Loud"
            TIME_SETTING_UNSET -> text_addition = "Unset"
        }

        item.setText(" - ${volumeState.getFormattedStartDate()}: $text_addition")

        outerLayout.addView(item)
        return outerLayout
    }

    private fun setLegendColor(color: Int, image: ImageView): View {
        val shapeDrawable = resources.getDrawable(R.drawable.shape_drawable_bar_legend) as GradientDrawable
        shapeDrawable.color = ColorStateList.valueOf(resources.getColor(color))
        image.setImageDrawable(shapeDrawable)

        return image
    }


    /**
     * Required to dismiss old tooltips when a new was opened
     */
    fun handleTooltipRight(context: Context, tooltip: String, view: View){
        view.showAlignLeft(getTooltip(context, tooltip, ArrowOrientation.RIGHT).build())
    }


    fun getTooltip(context: Context, tooltip: String): Balloon.Builder {
        return getTooltip(context, tooltip, ArrowOrientation.BOTTOM)
    }

    fun getTooltip(context: Context, tooltip: String, orientation: ArrowOrientation): Balloon.Builder {
        var balloon = Balloon.Builder(context)
        balloon.setArrowSize(10)
        balloon.setArrowPosition(0.5f)
        balloon.setCornerRadius(4f)
        //balloon.setHeight(getSizeInDP(12))
        balloon.paddingLeft=SizeUtil.getSizeInDP(context, 8)
        balloon.paddingRight=SizeUtil.getSizeInDP(context, 8)
        balloon.paddingTop=SizeUtil.getSizeInDP(context, 8)
        balloon.paddingBottom=SizeUtil.getSizeInDP(context, 8)
        balloon.setAlpha(0.9f)
        balloon.setText(tooltip)
        balloon.setArrowOrientation(orientation)
        balloon.setBalloonAnimation(BalloonAnimation.OVERSHOOT)
        balloon.setBackgroundColor(ContextCompat.getColor(context, R.color.tooltip_background))
        balloon.setDismissWhenTouchOutside(true)
        return balloon
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun getColorFromState(state: Int): Int {
        var id= R.color.color_graph_unset
        when (state) {
            TIME_SETTING_SILENT -> id = R.color.color_graph_silent
            TIME_SETTING_VIBRATE -> id = R.color.color_graph_vibrate
            TIME_SETTING_LOUD -> id = R.color.color_graph_loud
        }
        return id
    }
}
