package com.viatom.checkme.leftnavi.ecgRecorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.viatom.checkme.Chanl
import com.viatom.checkme.R
import com.viatom.checkme.activity.MainActivity
import com.viatom.checkme.ble.format.DlcFile
import com.viatom.checkme.ble.format.EcgFile
import com.viatom.checkme.leftnavi.dailyCheck.DailyCheckViewModel
import com.viatom.checkme.leftnavi.dailyCheck.DailyViewAdapter
import com.viatom.checkme.utils.Constant
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File

class EcgRecorderFragment : Fragment() {

    private val model: EcgRecorderViewModel by viewModels()
    lateinit var ecgViewAdapter: EcgViewAdapter
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_ecg, container, false)
        val r:RecyclerView=root.findViewById(R.id.ecglist)
        val linearLayoutManager= LinearLayoutManager(context)
        linearLayoutManager.orientation=RecyclerView.VERTICAL
        ecgViewAdapter= EcgViewAdapter(context,r)
        r.layoutManager=linearLayoutManager
        r.adapter=ecgViewAdapter
        model.list.observe(viewLifecycleOwner,{
            ecgViewAdapter.addAll(it)
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
            val temp= File(Constant.getPathX("1ecg.dat")).readBytes()
            if(!temp.isEmpty()) {
                temp.let {
                    val f = EcgFile.EcgInfo(it)
                    model.list.value = f.ecg
                }
            }

        }
    }
}