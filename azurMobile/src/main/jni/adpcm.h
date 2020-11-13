/*
** adpcm.h - include file for adpcm coder.
**
** Version 1.0, 7-Jul-92.
*/

#ifndef _ADPCM_H_
#define _ADPCM_H_

#include <STDIO.H>

typedef struct adpcm_state_t {
    short	valprev;	/* Previous output value */
    char	index;		/* Index into stepsize table */
}adpcm_state;

#define ID_RIFF							(0x46464952)
#define ID_WAVE							(0x45564157)
#define ID_FMT							(0x20746d66)
#define ID_FACT							(0x74636166)
#define ID_DATA							(0x61746164)
#define MAX_BYTES						(1024)

#define _DEBUG_							(1)

#if		_DEBUG_
#define print(x,fmt)					{printf ("%-20s = "#fmt,#x,x);}
#else
#define print(x,fmt)		
#endif

/* configure size carefully 
 * usually,256 size is commonly supported by PC and MOblie device 
 */
#define BLOCK_SIZE						(256 )
#define SMPS_PER_BLOCK					((BLOCK_SIZE - 4) * 2 + 1)
extern short										s_buffer [BLOCK_SIZE<<1];

/* intel ADPCM */
struct IMA_hdr {
	unsigned	r_id;					/* 'RIFF' */
	unsigned	r_size;					/* size after here */
	unsigned	r_type;					/* riff type */
	
	unsigned	ft_id;					/* 'fmt ' */
	unsigned	ft_size;				/* chunk size */
	short		ft_tag;					/* PCM / ADPCM */
	short		ft_channel;				/* 1/2 channels */
	unsigned	ft_smprate;				/* samples rate */
	unsigned	ft_bytesrate;			/* bytes rate */
	short		ft_blkalign;			/* block align */
	short		ft_bits_per_smp;		/* bit width */
	short		ft_cbsize;				/* extral size */
	short		ft_smp_per_blk;			/* smps per block */
	
	unsigned	fc_id ;					/* 'fact' */
	unsigned	fc_size;				/* fact block size */
	unsigned	fc_timelen;				/* time length */
	
	unsigned	d_id;					/* data chunk id */
	unsigned	d_size;					/* data chunk size */
};

/* windows raw PCM linear data */
struct PCM_hdr {
	unsigned	r_id;					/* 'RIFF' */
	unsigned	r_size;					/* size after here */
	unsigned	r_type;					/* riff type */
	
	unsigned	ft_id;					/* 'fmt ' */
	unsigned	ft_size;				/* chunk size */
	short		ft_tag;					/* PCM / ADPCM */
	short		ft_channel;				/* 1/2 channels */
	unsigned	ft_smprate;				/* samples rate */
	unsigned	ft_bytesrate;			/* bytes rate */
	short		ft_blkalign;			/* block align */
	short		ft_bits_per_smp;		/* bit width */
	
	unsigned	d_id;					/* data chunk id */
	unsigned	d_size;					/* data chunk size */
};

struct block_hdr {
	short		smp;
	char		index;
	char		reserved;
	char		dat [BLOCK_SIZE - 4];
};

#ifdef __cplusplus 
extern "C" {
#endif
	extern void adpcm_coder(short *indata, char *outdata, int len, int inc,adpcm_state *state);
	extern void adpcm_decoder(char *indata, short *outdata, int len, int inc,adpcm_state *state);

	extern int pcm2adpcm (const char *infile,const char *outfile) ;
	extern int adpcm2pcm (const char *infile,const char *outfile) ;

#ifdef __cplusplus
}
#endif



/* end of file */
#endif

