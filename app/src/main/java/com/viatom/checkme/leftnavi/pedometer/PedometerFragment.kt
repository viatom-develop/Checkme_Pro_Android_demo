package com.viatom.checkme.leftnavi.pedometer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.viatom.checkme.Chanl
import com.viatom.checkme.R
import com.viatom.checkme.activity.MainActivity
import com.viatom.checkme.ble.format.DlcFile
import com.viatom.checkme.ble.format.OxyFile
import com.viatom.checkme.ble.format.PedFile
import com.viatom.checkme.leftnavi.dailyCheck.DailyCheckViewModel
import com.viatom.checkme.leftnavi.dailyCheck.DailyViewAdapter
import com.viatom.checkme.leftnavi.pulseOximeter.OxyViewAdapter
import com.viatom.checkme.utils.Constant
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File


class PedometerFragment : Fragment() {

    private val model: PedViewModel by viewModels()
    lateinit var pedViewAdapter: PedViewAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_pedometer, container, false)
        val r: RecyclerView =root.findViewById(R.id.pedlist)
        val linearLayoutManager= LinearLayoutManager(context)
        linearLayoutManager.orientation= RecyclerView.VERTICAL
        pedViewAdapter= PedViewAdapter(context,r)
        r.layoutManager=linearLayoutManager
        r.adapter=pedViewAdapter
        model.list.observe(viewLifecycleOwner,{
           pedViewAdapter.addAll(it)
        })

        return root
    }
    @ExperimentalUnsignedTypes
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        MainScope().launch {
            if(MainActivity.loading){
                Chanl.teChannel.receive()
            }
            val file= File(Constant.getPathX("1ped.dat"))
            if(file.exists()){
                val temp=file.readBytes()
                temp.let {
                    val f = PedFile.PedInfo(it)
                    model.list.value = f.Ped
                }
            }
        }
    }

}