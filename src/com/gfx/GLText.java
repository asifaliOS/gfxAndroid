
package com.gfx;

import javax.microedition.khronos.opengles.GL10;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class GLText {

   //--Constants--//
   public final static int CHAR_START = 32;           // First Character (ASCII Code)
   public final static int CHAR_END = 126;            // Last Character (ASCII Code)
   public final static int CHAR_CNT = ( ( ( CHAR_END - CHAR_START ) + 1 ) + 1 );  // Character Count (Including Character to use for Unknown)

   public final static int CHAR_NONE = 32;            // Character to Use for Unknown (ASCII Code)
   public final static int CHAR_UNKNOWN = ( CHAR_CNT - 1 );  // Index of the Unknown Character

   public final static int FONT_SIZE_MIN = 6;         // Minumum Font Size (Pixels)
   public final static int FONT_SIZE_MAX = 180;       // Maximum Font Size (Pixels)

   public final static int CHAR_BATCH_SIZE = 100;     // Number of Characters to Render Per Batch

   //--Members--//
   GL10 gl;                                           // GL10 Instance
   AssetManager assets;                               // Asset Manager
   SpriteBatch batch;                                 // Batch Renderer

   int fontPadX, fontPadY;                            // Font Padding (Pixels; On Each Side, ie. Doubled on Both X+Y Axis)

   float fontHeight;                                  // Font Height (Actual; Pixels)
   float fontAscent;                                  // Font Ascent (Above Baseline; Pixels)
   float fontDescent;                                 // Font Descent (Below Baseline; Pixels)

   int textureId;                                     // Font Texture ID [NOTE: Public for Testing Purposes Only!]
   int textureSize;                                   // Texture Size for Font (Square) [NOTE: Public for Testing Purposes Only!]
   TextureRegion textureRgn;                          // Full Texture Region

   float charWidthMax;                                // Character Width (Maximum; Pixels)
   float charHeight;                                  // Character Height (Maximum; Pixels)
   final float[] charWidths;                          // Width of Each Character (Actual; Pixels)
   TextureRegion[] charRgn;                           // Region of Each Character (Texture Coordinates)
   int cellWidth, cellHeight;                         // Character Cell Width/Height
   int rowCnt, colCnt;                                // Number of Rows/Columns

   float scaleX, scaleY;                              // Font Scale (X,Y Axis)
   float spaceX;                                      // Additional (X,Y Axis) Spacing (Unscaled)


   //--Constructor--//
   // D: save GL instance + asset manager, create arrays, and initialize the members
   // A: gl - OpenGL ES 10 Instance
   public GLText(GL10 gl, AssetManager assets) {
      this.gl = gl;                                   // Save the GL10 Instance
      this.assets = assets;                           // Save the Asset Manager Instance

      batch = new SpriteBatch( gl, CHAR_BATCH_SIZE );  // Create Sprite Batch (with Defined Size)

      charWidths = new float[CHAR_CNT];               // Create the Array of Character Widths
      charRgn = new TextureRegion[CHAR_CNT];          // Create the Array of Character Regions

      // initialize remaining members
      fontPadX = 0;
      fontPadY = 0;

      fontHeight = 0.0f;
      fontAscent = 0.0f;
      fontDescent = 0.0f;

      textureId = -1;
      textureSize = 0;

      charWidthMax = 0;
      charHeight = 0;

      cellWidth = 0;
      cellHeight = 0;
      rowCnt = 0;
      colCnt = 0;

      scaleX = 1.0f;                                  // Default Scale = 1 (Unscaled)
      scaleY = 1.0f;                                  // Default Scale = 1 (Unscaled)
      spaceX = 0.0f;
   }

   //--Load Font--//
   // description
   //    this will load the specified font file, create a texture for the defined
   //    character range, and setup all required values used to render with it.
   // arguments:
   //    file - Filename of the font (.ttf, .otf) to use. In 'Assets' folder.
   //    size - Requested pixel size of font (height)
   //    padX, padY - Extra padding per character (X+Y Axis); to prevent overlapping characters.
   public boolean load(String file, int size, int padX, int padY) {

      // setup requested values
      fontPadX = padX;                                // Set Requested X Axis Padding
      fontPadY = padY;                                // Set Requested Y Axis Padding

      // load the font and setup paint instance for drawing
      Typeface tf = Typeface.createFromAsset( assets, file );  // Create the Typeface from Font File
      Paint paint = new Paint();                      // Create Android Paint Instance
      paint.setAntiAlias( true );                     // Enable Anti Alias
      paint.setTextSize( size );                      // Set Text Size
      paint.setColor( 0xffffffff );                   // Set ARGB (White, Opaque)
      paint.setTypeface( tf );                        // Set Typeface

      // get font metrics
      Paint.FontMetrics fm = paint.getFontMetrics();  // Get Font Metrics
      fontHeight = (float)Math.ceil( Math.abs( fm.bottom ) + Math.abs( fm.top ) );  // Calculate Font Height
      fontAscent = (float)Math.ceil( Math.abs( fm.ascent ) );  // Save Font Ascent
      fontDescent = (float)Math.ceil( Math.abs( fm.descent ) );  // Save Font Descent

      // determine the width of each character (including unknown character)
      // also determine the maximum character width
      char[] s = new char[2];                         // Create Character Array
      charWidthMax = charHeight = 0;                  // Reset Character Width/Height Maximums
      float[] w = new float[2];                       // Working Width Value
      int cnt = 0;                                    // Array Counter
      for ( char c = CHAR_START; c <= CHAR_END; c++ )  {  // FOR Each Character
         s[0] = c;                                    // Set Character
         paint.getTextWidths( s, 0, 1, w );           // Get Character Bounds
         charWidths[cnt] = w[0];                      // Get Width
         if ( charWidths[cnt] > charWidthMax )        // IF Width Larger Than Max Width
            charWidthMax = charWidths[cnt];           // Save New Max Width
         cnt++;                                       // Advance Array Counter
      }
      s[0] = CHAR_NONE;                               // Set Unknown Character
      paint.getTextWidths( s, 0, 1, w );              // Get Character Bounds
      charWidths[cnt] = w[0];                         // Get Width
      if ( charWidths[cnt] > charWidthMax )           // IF Width Larger Than Max Width
         charWidthMax = charWidths[cnt];              // Save New Max Width
      cnt++;                                          // Advance Array Counter

      // set character height to font height
      charHeight = fontHeight;                        // Set Character Height

      // find the maximum size, validate, and setup cell sizes
      cellWidth = (int)charWidthMax + ( 2 * fontPadX );  // Set Cell Width
      cellHeight = (int)charHeight + ( 2 * fontPadY );  // Set Cell Height
      int maxSize = cellWidth > cellHeight ? cellWidth : cellHeight;  // Save Max Size (Width/Height)
      if ( maxSize < FONT_SIZE_MIN || maxSize > FONT_SIZE_MAX )  // IF Maximum Size Outside Valid Bounds
         return false;                                // Return Error

      // set texture size based on max font size (width or height)
      // NOTE: these values are fixed, based on the defined characters. when
      // changing start/end characters (CHAR_START/CHAR_END) this will need adjustment too!
      if ( maxSize <= 24 )                            // IF Max Size is 18 or Less
         textureSize = 256;                           // Set 256 Texture Size
      else if ( maxSize <= 40 )                       // ELSE IF Max Size is 40 or Less
         textureSize = 512;                           // Set 512 Texture Size
      else if ( maxSize <= 80 )                       // ELSE IF Max Size is 80 or Less
         textureSize = 1024;                          // Set 1024 Texture Size
      else                                            // ELSE IF Max Size is Larger Than 80 (and Less than FONT_SIZE_MAX)
         textureSize = 2048;                          // Set 2048 Texture Size

      // create an empty bitmap (alpha only)
      Bitmap bitmap = Bitmap.createBitmap( textureSize, textureSize, Bitmap.Config.ALPHA_8 );  // Create Bitmap
      Canvas canvas = new Canvas( bitmap );           // Create Canvas for Rendering to Bitmap
      bitmap.eraseColor( 0x00000000 );                // Set Transparent Background (ARGB)

      // calculate rows/columns
      // NOTE: while not required for anything, these may be useful to have :)
      colCnt = textureSize / cellWidth;               // Calculate Number of Columns
      rowCnt = (int)Math.ceil( (float)CHAR_CNT / (float)colCnt );  // Calculate Number of Rows

      // render each of the characters to the canvas (ie. build the font map)
      float x = fontPadX;                             // Set Start Position (X)
      float y = ( cellHeight - 1 ) - fontDescent - fontPadY;  // Set Start Position (Y)
      for ( char c = CHAR_START; c <= CHAR_END; c++ )  {  // FOR Each Character
         s[0] = c;                                    // Set Character to Draw
         canvas.drawText( s, 0, 1, x, y, paint );     // Draw Character
         x += cellWidth;                              // Move to Next Character
         if ( ( x + cellWidth - fontPadX ) > textureSize )  {  // IF End of Line Reached
            x = fontPadX;                             // Set X for New Row
            y += cellHeight;                          // Move Down a Row
         }
      }
      s[0] = CHAR_NONE;                               // Set Character to Use for NONE
      canvas.drawText( s, 0, 1, x, y, paint );        // Draw Character

      // generate a new texture
      int[] textureIds = new int[1];                  // Array to Get Texture Id
      gl.glGenTextures( 1, textureIds, 0 );           // Generate New Texture
      textureId = textureIds[0];                      // Save Texture Id

      // setup filters for texture
      gl.glBindTexture( GL10.GL_TEXTURE_2D, textureId );  // Bind Texture
      gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST );  // Set Minification Filter
      gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR );  // Set Magnification Filter
      gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE );  // Set U Wrapping
      gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE );  // Set V Wrapping

      // load the generated bitmap onto the texture
      GLUtils.texImage2D( GL10.GL_TEXTURE_2D, 0, bitmap, 0 );  // Load Bitmap to Texture
      gl.glBindTexture( GL10.GL_TEXTURE_2D, 0 );      // Unbind Texture

      // release the bitmap
      bitmap.recycle();                               // Release the Bitmap

      // setup the array of character texture regions
      x = 0;                                          // Initialize X
      y = 0;                                          // Initialize Y
      for ( int c = 0; c < CHAR_CNT; c++ )  {         // FOR Each Character (On Texture)
         charRgn[c] = new TextureRegion( textureSize, textureSize, x, y, cellWidth-1, cellHeight-1 );  // Create Region for Character
         x += cellWidth;                              // Move to Next Char (Cell)
         if ( x + cellWidth > textureSize )  {
            x = 0;                                    // Reset X Position to Start
            y += cellHeight;                          // Move to Next Row (Cell)
         }
      }

      // create full texture region
      textureRgn = new TextureRegion( textureSize, textureSize, 0, 0, textureSize, textureSize );  // Create Full Texture Region

      // return success
      return true;                                    // Return Success
   }

   //--Begin/End Text Drawing--//
   // D: call these methods before/after (respectively all draw() calls using a text instance
   //    NOTE: color is set on a per-batch basis, and fonts should be 8-bit alpha only!!!
   // A: red, green, blue - RGB values for font (default = 1.0)
   //    alpha - optional alpha value for font (default = 1.0)
   // R: [none]
   public void begin()  {
      begin( 1.0f, 1.0f, 1.0f, 1.0f );                // Begin with White Opaque
   }
   public void begin(float alpha)  {
      begin( 1.0f, 1.0f, 1.0f, alpha );               // Begin with White (Explicit Alpha)
   }
   public void begin(float red, float green, float blue, float alpha)  {
      gl.glColor4f( red, green, blue, alpha );        // Set Color+Alpha
      gl.glBindTexture( GL10.GL_TEXTURE_2D, textureId );  // Bind the Texture
      batch.beginBatch();                             // Begin Batch
   }
   public void end()  {
      batch.endBatch();                               // End Batch
      gl.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );         // Restore Default Color/Alpha
   }

   //--Draw Text--//
   // D: draw text at the specified x,y position
   // A: text - the string to draw
   //    x, y - the x,y position to draw text at (bottom left of text; including descent)
   // R: [none]
   public void draw(String text, float x, float y)  {
      float chrHeight = cellHeight * scaleY;          // Calculate Scaled Character Height
      float chrWidth = cellWidth * scaleX;            // Calculate Scaled Character Width
      int len = text.length();                        // Get String Length
      x += ( chrWidth / 2.0f ) - ( fontPadX * scaleX );  // Adjust Start X
      y += ( chrHeight / 2.0f ) - ( fontPadY * scaleY );  // Adjust Start Y
      for ( int i = 0; i < len; i++ )  {              // FOR Each Character in String
         int c = (int)text.charAt( i ) - CHAR_START;  // Calculate Character Index (Offset by First Char in Font)
         if ( c < 0 || c >= CHAR_CNT )                // IF Character Not In Font
            c = CHAR_UNKNOWN;                         // Set to Unknown Character Index
         batch.drawSprite( x, y, chrWidth, chrHeight, charRgn[c] );  // Draw the Character
         x += ( charWidths[c] + spaceX ) * scaleX;    // Advance X Position by Scaled Character Width
      }
   }

   //--Draw Text Centered--//
   // D: draw text CENTERED at the specified x,y position
   // A: text - the string to draw
   //    x, y - the x,y position to draw text at (bottom left of text)
   // R: the total width of the text that was drawn
   public float drawC(String text, float x, float y)  {
      float len = getLength( text );                  // Get Text Length
      draw( text, x - ( len / 2.0f ), y - ( getCharHeight() / 2.0f ) );  // Draw Text Centered
      return len;                                     // Return Length
   }
   public float drawCX(String text, float x, float y)  {
      float len = getLength( text );                  // Get Text Length
      draw( text, x - ( len / 2.0f ), y );            // Draw Text Centered (X-Axis Only)
      return len;                                     // Return Length
   }
   public void drawCY(String text, float x, float y)  {
      draw( text, x, y - ( getCharHeight() / 2.0f ) );  // Draw Text Centered (Y-Axis Only)
   }

   //--Set Scale--//
   // D: set the scaling to use for the font
   // A: scale - uniform scale for both x and y axis scaling
   //    sx, sy - separate x and y axis scaling factors
   // R: [none]
   public void setScale(float scale)  {
      scaleX = scaleY = scale;                        // Set Uniform Scale
   }
   public void setScale(float sx, float sy)  {
      scaleX = sx;                                    // Set X Scale
      scaleY = sy;                                    // Set Y Scale
   }

   //--Get Scale--//
   // D: get the current scaling used for the font
   // A: [none]
   // R: the x/y scale currently used for scale
   public float getScaleX()  {
      return scaleX;                                  // Return X Scale
   }
   public float getScaleY()  {
      return scaleY;                                  // Return Y Scale
   }

   //--Set Space--//
   // D: set the spacing (unscaled; ie. pixel size) to use for the font
   // A: space - space for x axis spacing
   // R: [none]
   public void setSpace(float space)  {
      spaceX = space;                                 // Set Space
   }

   //--Get Space--//
   // D: get the current spacing used for the font
   // A: [none]
   // R: the x/y space currently used for scale
   public float getSpace()  {
      return spaceX;                                  // Return X Space
   }

   //--Get Length of a String--//
   // D: return the length of the specified string if rendered using current settings
   // A: text - the string to get length for
   // R: the length of the specified string (pixels)
   public float getLength(String text) {
      float len = 0.0f;                               // Working Length
      int strLen = text.length();                     // Get String Length (Characters)
      for ( int i = 0; i < strLen; i++ )  {           // For Each Character in String (Except Last
         int c = (int)text.charAt( i ) - CHAR_START;  // Calculate Character Index (Offset by First Char in Font)
         len += ( charWidths[c] * scaleX );           // Add Scaled Character Width to Total Length
      }
      len += ( strLen > 1 ? ( ( strLen - 1 ) * spaceX ) * scaleX : 0 );  // Add Space Length
      return len;                                     // Return Total Length
   }

   //--Get Width/Height of Character--//
   // D: return the scaled width/height of a character, or max character width
   //    NOTE: since all characters are the same height, no character index is required!
   //    NOTE: excludes spacing!!
   // A: chr - the character to get width for
   // R: the requested character size (scaled)
   public float getCharWidth(char chr)  {
      int c = chr - CHAR_START;                       // Calculate Character Index (Offset by First Char in Font)
      return ( charWidths[c] * scaleX );              // Return Scaled Character Width
   }
   public float getCharWidthMax()  {
      return ( charWidthMax * scaleX );               // Return Scaled Max Character Width
   }
   public float getCharHeight() {
      return ( charHeight * scaleY );                 // Return Scaled Character Height
   }

   //--Get Font Metrics--//
   // D: return the specified (scaled) font metric
   // A: [none]
   // R: the requested font metric (scaled)
   public float getAscent()  {
      return ( fontAscent * scaleY );                 // Return Font Ascent
   }
   public float getDescent()  {
      return ( fontDescent * scaleY );                // Return Font Descent
   }
   public float getHeight()  {
      return ( fontHeight * scaleY );                 // Return Font Height (Actual)
   }

   //--Draw Font Texture--//
   // D: draw the entire font texture (NOTE: for testing purposes only)
   // A: width, height - the width and height of the area to draw to. this is used
   //    to draw the texture to the top-left corner.
   public void drawTexture(int width, int height)  {
      batch.beginBatch( textureId );                  // Begin Batch (Bind Texture)
      batch.drawSprite( textureSize / 2, height - ( textureSize / 2 ), textureSize, textureSize, textureRgn );  // Draw
      batch.endBatch();                               // End Batch
   }

    //===============================================================================
    class SpriteBatch {

            //--Constants--//
            final static int VERTEX_SIZE = 4;                  // Vertex Size (in Components) ie. (X,Y,U,V)
            final static int VERTICES_PER_SPRITE = 4;          // Vertices Per Sprite
            final static int INDICES_PER_SPRITE = 6;           // Indices Per Sprite

            //--Members--//
            GL10 gl;                                           // GL Instance
            Vertices vertices;                                 // Vertices Instance Used for Rendering
            float[] vertexBuffer;                              // Vertex Buffer
            int bufferIndex;                                   // Vertex Buffer Start Index
            int maxSprites;                                    // Maximum Sprites Allowed in Buffer
            int numSprites;                                    // Number of Sprites Currently in Buffer

            //--Constructor--//
            // D: prepare the sprite batcher for specified maximum number of sprites
            // A: gl - the gl instance to use for rendering
            //    maxSprites - the maximum allowed sprites per batch
            public SpriteBatch(GL10 gl, int maxSprites)  {
                this.gl = gl;                                   // Save GL Instance
                this.vertexBuffer = new float[maxSprites * VERTICES_PER_SPRITE * VERTEX_SIZE];  // Create Vertex Buffer
                this.vertices = new Vertices( gl, maxSprites * VERTICES_PER_SPRITE, maxSprites * INDICES_PER_SPRITE, false, true, false );  // Create Rendering Vertices
                this.bufferIndex = 0;                           // Reset Buffer Index
                this.maxSprites = maxSprites;                   // Save Maximum Sprites
                this.numSprites = 0;                            // Clear Sprite Counter

                short[] indices = new short[maxSprites * INDICES_PER_SPRITE];  // Create Temp Index Buffer
                int len = indices.length;                       // Get Index Buffer Length
                short j = 0;                                    // Counter
                for ( int i = 0; i < len; i+= INDICES_PER_SPRITE, j += VERTICES_PER_SPRITE )  {  // FOR Each Index Set (Per Sprite)
                    indices[i + 0] = (short)( j + 0 );           // Calculate Index 0
                    indices[i + 1] = (short)( j + 1 );           // Calculate Index 1
                    indices[i + 2] = (short)( j + 2 );           // Calculate Index 2
                    indices[i + 3] = (short)( j + 2 );           // Calculate Index 3
                    indices[i + 4] = (short)( j + 3 );           // Calculate Index 4
                    indices[i + 5] = (short)( j + 0 );           // Calculate Index 5
                }
                vertices.setIndices( indices, 0, len );         // Set Index Buffer for Rendering
            }

            //--Begin Batch--//
            // D: signal the start of a batch. set the texture and clear buffer
            //    NOTE: the overloaded (non-texture) version assumes that the texture is already bound!
            // A: textureId - the ID of the texture to use for the batch
            // R: [none]
            public void beginBatch(int textureId)  {
                gl.glBindTexture( GL10.GL_TEXTURE_2D, textureId );  // Bind the Texture
                numSprites = 0;                                 // Empty Sprite Counter
                bufferIndex = 0;                                // Reset Buffer Index (Empty)
            }
            public void beginBatch()  {
                numSprites = 0;                                 // Empty Sprite Counter
                bufferIndex = 0;                                // Reset Buffer Index (Empty)
            }

            //--End Batch--//
            // D: signal the end of a batch. render the batched sprites
            // A: [none]
            // R: [none]
            public void endBatch()  {
                if ( numSprites > 0 )  {                        // IF Any Sprites to Render
                    vertices.setVertices( vertexBuffer, 0, bufferIndex );  // Set Vertices from Buffer
                    vertices.bind();                             // Bind Vertices
                    vertices.draw( GL10.GL_TRIANGLES, 0, numSprites * INDICES_PER_SPRITE );  // Render Batched Sprites
                    vertices.unbind();                           // Unbind Vertices
                }
            }

            //--Draw Sprite to Batch--//
            // D: batch specified sprite to batch. adds vertices for sprite to vertex buffer
            //    NOTE: MUST be called after beginBatch(), and before endBatch()!
            //    NOTE: if the batch overflows, this will render the current batch, restart it,
            //          and then batch this sprite.
            // A: x, y - the x,y position of the sprite (center)
            //    width, height - the width and height of the sprite
            //    region - the texture region to use for sprite
            // R: [none]
            public void drawSprite(float x, float y, float width, float height, TextureRegion region)  {
                if ( numSprites == maxSprites )  {              // IF Sprite Buffer is Full
                    endBatch();                                  // End Batch
                    // NOTE: leave current texture bound!!
                    numSprites = 0;                              // Empty Sprite Counter
                    bufferIndex = 0;                             // Reset Buffer Index (Empty)
                }

                float halfWidth = width / 2.0f;                 // Calculate Half Width
                float halfHeight = height / 2.0f;               // Calculate Half Height
                float x1 = x - halfWidth;                       // Calculate Left X
                float y1 = y - halfHeight;                      // Calculate Bottom Y
                float x2 = x + halfWidth;                       // Calculate Right X
                float y2 = y + halfHeight;                      // Calculate Top Y

                vertexBuffer[bufferIndex++] = x1;               // Add X for Vertex 0
                vertexBuffer[bufferIndex++] = y1;               // Add Y for Vertex 0
                vertexBuffer[bufferIndex++] = region.u1;        // Add U for Vertex 0
                vertexBuffer[bufferIndex++] = region.v2;        // Add V for Vertex 0

                vertexBuffer[bufferIndex++] = x2;               // Add X for Vertex 1
                vertexBuffer[bufferIndex++] = y1;               // Add Y for Vertex 1
                vertexBuffer[bufferIndex++] = region.u2;        // Add U for Vertex 1
                vertexBuffer[bufferIndex++] = region.v2;        // Add V for Vertex 1

                vertexBuffer[bufferIndex++] = x2;               // Add X for Vertex 2
                vertexBuffer[bufferIndex++] = y2;               // Add Y for Vertex 2
                vertexBuffer[bufferIndex++] = region.u2;        // Add U for Vertex 2
                vertexBuffer[bufferIndex++] = region.v1;        // Add V for Vertex 2

                vertexBuffer[bufferIndex++] = x1;               // Add X for Vertex 3
                vertexBuffer[bufferIndex++] = y2;               // Add Y for Vertex 3
                vertexBuffer[bufferIndex++] = region.u1;        // Add U for Vertex 3
                vertexBuffer[bufferIndex++] = region.v1;        // Add V for Vertex 3

                numSprites++;                                   // Increment Sprite Count
            }
        }
    //====================================================================================
    class TextureRegion {

        //--Members--//
        public float u1, v1;                               // Top/Left U,V Coordinates
        public float u2, v2;                               // Bottom/Right U,V Coordinates

        //--Constructor--//
        // D: calculate U,V coordinates from specified texture coordinates
        // A: texWidth, texHeight - the width and height of the texture the region is for
        //    x, y - the top/left (x,y) of the region on the texture (in pixels)
        //    width, height - the width and height of the region on the texture (in pixels)
        public TextureRegion(float texWidth, float texHeight, float x, float y, float width, float height)  {
            this.u1 = x / texWidth;                         // Calculate U1
            this.v1 = y / texHeight;                        // Calculate V1
            this.u2 = this.u1 + ( width / texWidth );       // Calculate U2
            this.v2 = this.v1 + ( height / texHeight );     // Calculate V2
        }
    }
    //===========================================================================================================
    public class Vertices {

        //--Constants--//
        final static int POSITION_CNT_2D = 2;              // Number of Components in Vertex Position for 2D
        final static int POSITION_CNT_3D = 3;              // Number of Components in Vertex Position for 3D
        final static int COLOR_CNT = 4;                    // Number of Components in Vertex Color
        final static int TEXCOORD_CNT = 2;                 // Number of Components in Vertex Texture Coords
        final static int NORMAL_CNT = 3;                   // Number of Components in Vertex Normal

        final static int INDEX_SIZE = Short.SIZE / 8;      // Index Byte Size (Short.SIZE = bits)

        //--Members--//
        // NOTE: all members are constant, and initialized in constructor!
        final GL10 gl;                                     // GL Instance
        final boolean hasColor;                            // Use Color in Vertices
        final boolean hasTexCoords;                        // Use Texture Coords in Vertices
        final boolean hasNormals;                          // Use Normals in Vertices
        public final int positionCnt;                      // Number of Position Components (2=2D, 3=3D)
        public final int vertexStride;                     // Vertex Stride (Element Size of a Single Vertex)
        public final int vertexSize;                       // Bytesize of a Single Vertex
        final IntBuffer vertices;                          // Vertex Buffer
        final ShortBuffer indices;                         // Index Buffer
        public int numVertices;                            // Number of Vertices in Buffer
        public int numIndices;                             // Number of Indices in Buffer
        final int[] tmpBuffer;                             // Temp Buffer for Vertex Conversion

        //--Constructor--//
        // D: create the vertices/indices as specified (for 2d/3d)
        // A: gl - the gl instance to use
        //    maxVertices - maximum vertices allowed in buffer
        //    maxIndices - maximum indices allowed in buffer
        //    hasColor - use color values in vertices
        //    hasTexCoords - use texture coordinates in vertices
        //    hasNormals - use normals in vertices
        //    use3D - (false, default) use 2d positions (ie. x/y only)
        //            (true) use 3d positions (ie. x/y/z)
        public Vertices(GL10 gl, int maxVertices, int maxIndices, boolean hasColor, boolean hasTexCoords, boolean hasNormals)  {
            this( gl, maxVertices, maxIndices, hasColor, hasTexCoords, hasNormals, false );  // Call Overloaded Constructor
        }
        public Vertices(GL10 gl, int maxVertices, int maxIndices, boolean hasColor, boolean hasTexCoords, boolean hasNormals, boolean use3D)  {
            this.gl = gl;                                   // Save GL Instance
            this.hasColor = hasColor;                       // Save Color Flag
            this.hasTexCoords = hasTexCoords;               // Save Texture Coords Flag
            this.hasNormals = hasNormals;                   // Save Normals Flag
            this.positionCnt = use3D ? POSITION_CNT_3D : POSITION_CNT_2D;  // Set Position Component Count
            this.vertexStride = this.positionCnt + ( hasColor ? COLOR_CNT : 0 ) + ( hasTexCoords ? TEXCOORD_CNT : 0 ) + ( hasNormals ? NORMAL_CNT : 0 );  // Calculate Vertex Stride
            this.vertexSize = this.vertexStride * 4;        // Calculate Vertex Byte Size

            ByteBuffer buffer = ByteBuffer.allocateDirect( maxVertices * vertexSize );  // Allocate Buffer for Vertices (Max)
            buffer.order( ByteOrder.nativeOrder() );        // Set Native Byte Order
            this.vertices = buffer.asIntBuffer();           // Save Vertex Buffer

            if ( maxIndices > 0 )  {                        // IF Indices Required
                buffer = ByteBuffer.allocateDirect( maxIndices * INDEX_SIZE );  // Allocate Buffer for Indices (MAX)
                buffer.order( ByteOrder.nativeOrder() );     // Set Native Byte Order
                this.indices = buffer.asShortBuffer();       // Save Index Buffer
            }
            else                                            // ELSE Indices Not Required
                indices = null;                              // No Index Buffer

            numVertices = 0;                                // Zero Vertices in Buffer
            numIndices = 0;                                 // Zero Indices in Buffer

            this.tmpBuffer = new int[maxVertices * vertexSize / 4];  // Create Temp Buffer
        }

        //--Set Vertices--//
        // D: set the specified vertices in the vertex buffer
        //    NOTE: optimized to use integer buffer!
        // A: vertices - array of vertices (floats) to set
        //    offset - offset to first vertex in array
        //    length - number of floats in the vertex array (total)
        //             for easy setting use: vtx_cnt * (this.vertexSize / 4)
        // R: [none]
        public void setVertices(float[] vertices, int offset, int length)  {
            this.vertices.clear();                          // Remove Existing Vertices
            int last = offset + length;                     // Calculate Last Element
            for ( int i = offset, j = 0; i < last; i++, j++ )  // FOR Each Specified Vertex
                tmpBuffer[j] = Float.floatToRawIntBits( vertices[i] );  // Set Vertex as Raw Integer Bits in Buffer
            this.vertices.put( tmpBuffer, 0, length );      // Set New Vertices
            this.vertices.flip();                           // Flip Vertex Buffer
            this.numVertices = length / this.vertexStride;  // Save Number of Vertices
            //this.numVertices = length / ( this.vertexSize / 4 );  // Save Number of Vertices
        }

        //--Set Indices--//
        // D: set the specified indices in the index buffer
        // A: indices - array of indices (shorts) to set
        //    offset - offset to first index in array
        //    length - number of indices in array (from offset)
        // R: [none]
        public void setIndices(short[] indices, int offset, int length)  {
            this.indices.clear();                           // Clear Existing Indices
            this.indices.put( indices, offset, length );    // Set New Indices
            this.indices.flip();                            // Flip Index Buffer
            this.numIndices = length;                       // Save Number of Indices
        }

        //--Bind--//
        // D: perform all required binding/state changes before rendering batches.
        //    USAGE: call once before calling draw() multiple times for this buffer.
        // A: [none]
        // R: [none]
        public void bind()  {
            gl.glEnableClientState( GL10.GL_VERTEX_ARRAY ); // Enable Position in Vertices
            vertices.position( 0 );                         // Set Vertex Buffer to Position
            gl.glVertexPointer( positionCnt, GL10.GL_FLOAT, vertexSize, vertices );  // Set Vertex Pointer

            if ( hasColor )  {                              // IF Vertices Have Color
                gl.glEnableClientState( GL10.GL_COLOR_ARRAY );  // Enable Color in Vertices
                vertices.position( positionCnt );            // Set Vertex Buffer to Color
                gl.glColorPointer( COLOR_CNT, GL10.GL_FLOAT, vertexSize, vertices );  // Set Color Pointer
            }

            if ( hasTexCoords )  {                          // IF Vertices Have Texture Coords
                gl.glEnableClientState( GL10.GL_TEXTURE_COORD_ARRAY );  // Enable Texture Coords in Vertices
                vertices.position( positionCnt + ( hasColor ? COLOR_CNT : 0 ) );  // Set Vertex Buffer to Texture Coords (NOTE: position based on whether color is also specified)
                gl.glTexCoordPointer( TEXCOORD_CNT, GL10.GL_FLOAT, vertexSize, vertices );  // Set Texture Coords Pointer
            }

            if ( hasNormals )  {
                gl.glEnableClientState( GL10.GL_NORMAL_ARRAY );  // Enable Normals in Vertices
                vertices.position( positionCnt + ( hasColor ? COLOR_CNT : 0 ) + ( hasTexCoords ? TEXCOORD_CNT : 0 ) );  // Set Vertex Buffer to Normals (NOTE: position based on whether color/texcoords is also specified)
                gl.glNormalPointer( GL10.GL_FLOAT, vertexSize, vertices );  // Set Normals Pointer
            }
        }

        //--Draw--//
        // D: draw the currently bound vertices in the vertex/index buffers
        //    USAGE: can only be called after calling bind() for this buffer.
        // A: primitiveType - the type of primitive to draw
        //    offset - the offset in the vertex/index buffer to start at
        //    numVertices - the number of vertices (indices) to draw
        // R: [none]
        public void draw(int primitiveType, int offset, int numVertices)  {
            if ( indices != null )  {                       // IF Indices Exist
                indices.position( offset );                  // Set Index Buffer to Specified Offset
                gl.glDrawElements( primitiveType, numVertices, GL10.GL_UNSIGNED_SHORT, indices );  // Draw Indexed
            }
            else  {                                         // ELSE No Indices Exist
                gl.glDrawArrays( primitiveType, offset, numVertices );  // Draw Direct (Array)
            }
        }

        //--Unbind--//
        // D: clear binding states when done rendering batches.
        //    USAGE: call once before calling draw() multiple times for this buffer.
        // A: [none]
        // R: [none]
        public void unbind()  {
            if ( hasColor )                                 // IF Vertices Have Color
                gl.glDisableClientState( GL10.GL_COLOR_ARRAY );  // Clear Color State

            if ( hasTexCoords )                             // IF Vertices Have Texture Coords
                gl.glDisableClientState( GL10.GL_TEXTURE_COORD_ARRAY );  // Clear Texture Coords State

            if ( hasNormals )                               // IF Vertices Have Normals
                gl.glDisableClientState( GL10.GL_NORMAL_ARRAY );  // Clear Normals State
        }

        //--Draw Full--//
        // D: draw the vertices in the vertex/index buffers
        //    NOTE: unoptimized version! use bind()/draw()/unbind() for batches
        // A: primitiveType - the type of primitive to draw
        //    offset - the offset in the vertex/index buffer to start at
        //    numVertices - the number of vertices (indices) to draw
        // R: [none]
        public void drawFull(int primitiveType, int offset, int numVertices)  {
            gl.glEnableClientState( GL10.GL_VERTEX_ARRAY ); // Enable Position in Vertices
            vertices.position( 0 );                         // Set Vertex Buffer to Position
            gl.glVertexPointer( positionCnt, GL10.GL_FLOAT, vertexSize, vertices );  // Set Vertex Pointer

            if ( hasColor )  {                              // IF Vertices Have Color
                gl.glEnableClientState( GL10.GL_COLOR_ARRAY );  // Enable Color in Vertices
                vertices.position( positionCnt );            // Set Vertex Buffer to Color
                gl.glColorPointer( COLOR_CNT, GL10.GL_FLOAT, vertexSize, vertices );  // Set Color Pointer
            }

            if ( hasTexCoords )  {                          // IF Vertices Have Texture Coords
                gl.glEnableClientState( GL10.GL_TEXTURE_COORD_ARRAY );  // Enable Texture Coords in Vertices
                vertices.position( positionCnt + ( hasColor ? COLOR_CNT : 0 ) );  // Set Vertex Buffer to Texture Coords (NOTE: position based on whether color is also specified)
                gl.glTexCoordPointer( TEXCOORD_CNT, GL10.GL_FLOAT, vertexSize, vertices );  // Set Texture Coords Pointer
            }

            if ( indices != null )  {                       // IF Indices Exist
                indices.position( offset );                  // Set Index Buffer to Specified Offset
                gl.glDrawElements( primitiveType, numVertices, GL10.GL_UNSIGNED_SHORT, indices );  // Draw Indexed
            }
            else  {                                         // ELSE No Indices Exist
                gl.glDrawArrays( primitiveType, offset, numVertices );  // Draw Direct (Array)
            }

            if ( hasTexCoords )                             // IF Vertices Have Texture Coords
                gl.glDisableClientState( GL10.GL_TEXTURE_COORD_ARRAY );  // Clear Texture Coords State

            if ( hasColor )                                 // IF Vertices Have Color
                gl.glDisableClientState( GL10.GL_COLOR_ARRAY );  // Clear Color State
        }

        //--Set Vertex Elements--//
        // D: use these methods to alter the values (position, color, textcoords, normals) for vertices
        //    WARNING: these do NOT validate any values, ensure that the index AND specified
        //             elements EXIST before using!!
        // A: x, y, z - the x,y,z position to set in buffer
        //    r, g, b, a - the r,g,b,a color to set in buffer
        //    u, v - the u,v texture coords to set in buffer
        //    nx, ny, nz - the x,y,z normal to set in buffer
        // R: [none]
        void setVtxPosition(int vtxIdx, float x, float y)  {
            int index = vtxIdx * vertexStride;              // Calculate Actual Index
            vertices.put( index + 0, Float.floatToRawIntBits( x ) );  // Set X
            vertices.put( index + 1, Float.floatToRawIntBits( y ) );  // Set Y
        }
        void setVtxPosition(int vtxIdx, float x, float y, float z)  {
            int index = vtxIdx * vertexStride;              // Calculate Actual Index
            vertices.put( index + 0, Float.floatToRawIntBits( x ) );  // Set X
            vertices.put( index + 1, Float.floatToRawIntBits( y ) );  // Set Y
            vertices.put( index + 2, Float.floatToRawIntBits( z ) );  // Set Z
        }
        void setVtxColor(int vtxIdx, float r, float g, float b, float a)  {
            int index = ( vtxIdx * vertexStride ) + positionCnt;  // Calculate Actual Index
            vertices.put( index + 0, Float.floatToRawIntBits( r ) );  // Set Red
            vertices.put( index + 1, Float.floatToRawIntBits( g ) );  // Set Green
            vertices.put( index + 2, Float.floatToRawIntBits( b ) );  // Set Blue
            vertices.put( index + 3, Float.floatToRawIntBits( a ) );  // Set Alpha
        }
        void setVtxColor(int vtxIdx, float r, float g, float b)  {
            int index = ( vtxIdx * vertexStride ) + positionCnt;  // Calculate Actual Index
            vertices.put( index + 0, Float.floatToRawIntBits( r ) );  // Set Red
            vertices.put( index + 1, Float.floatToRawIntBits( g ) );  // Set Green
            vertices.put( index + 2, Float.floatToRawIntBits( b ) );  // Set Blue
        }
        void setVtxColor(int vtxIdx, float a)  {
            int index = ( vtxIdx * vertexStride ) + positionCnt;  // Calculate Actual Index
            vertices.put( index + 3, Float.floatToRawIntBits( a ) );  // Set Alpha
        }
        void setVtxTexCoords(int vtxIdx, float u, float v)  {
            int index = ( vtxIdx * vertexStride ) + positionCnt + ( hasColor ? COLOR_CNT : 0 );  // Calculate Actual Index
            vertices.put( index + 0, Float.floatToRawIntBits( u ) );  // Set U
            vertices.put( index + 1, Float.floatToRawIntBits( v ) );  // Set V
        }
        void setVtxNormal(int vtxIdx, float x, float y, float z)  {
            int index = ( vtxIdx * vertexStride ) + positionCnt + ( hasColor ? COLOR_CNT : 0 ) + ( hasTexCoords ? TEXCOORD_CNT : 0 );  // Calculate Actual Index
            vertices.put( index + 0, Float.floatToRawIntBits( x ) );  // Set X
            vertices.put( index + 1, Float.floatToRawIntBits( y ) );  // Set Y
            vertices.put( index + 2, Float.floatToRawIntBits( z ) );  // Set Z
        }

}
}
