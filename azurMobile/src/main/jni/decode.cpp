#include <stdio.h>
#include <stdlib.H>
#include "adpcm.h"
#include "decode.h"
#include <STRING.H>
#include <STDLIB.H>

#include <android/log.h>
    #define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "VD", __VA_ARGS__)

#ifndef _DEBUG_
#define _DEBUG_
#endif

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

short	s_buffer [BLOCK_SIZE<<1];


JNIEXPORT jint JNICALL Java_com_viatom_azur_tools_AdpcmDecoder_decodeAdpcm (JNIEnv* env, jclass obj,  jstring infile, jstring outfile)
{
	LOGD("jump into decodeAdpcm!");

	const char* inFilePoint = (env)->GetStringUTFChars(infile, NULL);
	const char* outFilePoint = (env)->GetStringUTFChars(outfile, NULL);

//	LOGD("infile: %s",inFilePoint);
//	LOGD("outfile: %s",outFilePoint);

	FILE *infp = NULL, *outfp = NULL;
	struct PCM_hdr		pcm_hdr;
	struct IMA_hdr		ima_hdr;
	struct block_hdr	block;
	adpcm_state			state;
	int hdr_size		= sizeof(IMA_hdr),size = 0,cnt = 0,written = 0;

	if (!(infp = fopen (inFilePoint,"rb")) || !(outfp = fopen (outFilePoint,"wb+")))
		return (-1) ;

	/* try to read file header as IMA formate */
	fread ((void *) &ima_hdr,sizeof(ima_hdr),1,infp);

	/* formate check */
	if (   (ima_hdr.ft_tag != 0x11)
		|| (ima_hdr.ft_bits_per_smp != 0x04)
		|| (ima_hdr.ft_smp_per_blk	!= SMPS_PER_BLOCK)
		|| (ima_hdr.ft_channel != 1)) {
#if _DEBUG_
		printf ( "Only Intel ADPCM Wave formate is Supported !\n" );
#endif
		return (-2);
	}

	/* get file size */
	fseek (infp,0,SEEK_END);
	size = ftell (infp) - hdr_size ;

	/* fill codec */
	state.index = 0;
	state.valprev = 0;

	/* fill riff */
	pcm_hdr.r_id			= ID_RIFF;
	pcm_hdr.r_size			= 0;
	pcm_hdr.r_type			= ID_WAVE;

	/* fill fmt */
	pcm_hdr.ft_id			= ID_FMT;
	pcm_hdr.ft_size			= 0x10;		/* RAW */
	pcm_hdr.ft_tag			= 0x01;		/* PCM */
	pcm_hdr.ft_channel		= ima_hdr.ft_channel;
	pcm_hdr.ft_smprate		= ima_hdr.ft_smprate;
	pcm_hdr.ft_bits_per_smp	= 0x10;		/* 16 */
	pcm_hdr.ft_blkalign		= 2;
	pcm_hdr.ft_bytesrate	= (pcm_hdr.ft_smprate * pcm_hdr.ft_channel * pcm_hdr.ft_bits_per_smp) / 8 ;

	/* fill data */
	pcm_hdr.d_id			= ID_DATA;
	pcm_hdr.d_size			= 0;

	/* skip file header */
	fseek (infp,hdr_size,SEEK_SET);

	/* write data */
	while (size > 0) {
		if (size > BLOCK_SIZE)
			cnt = BLOCK_SIZE;
		else
			cnt = size;

		/* read block buffer data */
		fread ((void *) &block,sizeof (block),1,infp);

		/* get the first smp */
		state.index		= block.index;
		state.valprev	= block.smp;

		/* decode the rest of a block */
		adpcm_decoder ((char *) &block.dat,(short *) (s_buffer + 1),sizeof(block.dat) << 1,1,&state);

		s_buffer [0] = block.smp;

		/* write decoded data to dest file */
		fwrite ((const void *) s_buffer,((sizeof(block.dat) << 2) + 2),1,outfp);

		size -= cnt;
		written += ((sizeof(block.dat) << 2) + 2);
	}

	/* build PCM hdr */
	pcm_hdr.d_size				= written;
	pcm_hdr.r_size				= pcm_hdr.d_size + sizeof(pcm_hdr) - 8;

	/* write hdr */
	fseek (outfp,0,SEEK_SET);
	fwrite ((const void *) &pcm_hdr,sizeof(pcm_hdr),1,outfp);

	/* end decoding ... */
	fclose (infp);
	fclose (outfp);

	env->ReleaseStringUTFChars(infile, inFilePoint);
	env->ReleaseStringUTFChars(outfile, outFilePoint);
	return (0);
}


