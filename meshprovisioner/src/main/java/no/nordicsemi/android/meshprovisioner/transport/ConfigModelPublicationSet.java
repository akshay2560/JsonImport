/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.meshprovisioner.transport;

import android.support.annotation.NonNull;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.meshprovisioner.opcodes.ConfigMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.utils.AddressUtils;
import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * To be used as a wrapper class to create a ConfigModelPublicationSet message.
 */
@SuppressWarnings("unused")
public class ConfigModelPublicationSet extends ConfigMessage {

    private static final String TAG = ConfigModelPublicationSet.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_MODEL_PUBLICATION_SET;

    private static final int SIG_MODEL_PUBLISH_SET_PARAMS_LENGTH = 11;
    private static final int VENDOR_MODEL_PUBLISH_SET_PARAMS_LENGTH = 13;

    private final int elementAddress;
    private final int publishAddress;
    private final int appKeyIndex;
    private final boolean credentialFlag;
    private final int publishTtl;
    private final int publicationSteps;
    private final int publicationResolution;
    private final int publishRetransmitCount;
    private final int publishRetransmitIntervalSteps;
    private final int modelIdentifier;

    /**
     * Constructs a ConfigModelPublicationSet message
     *
     * @param elementAddress                 Element address that should publish
     * @param publishAddress                 Address to which the element must publish
     * @param appKeyIndex                    Index of the application key
     * @param credentialFlag                 Credentials flag define which credentials to be used, set true to use friendship credentials and false for master credentials.
     *                                       Currently supports only master credentials
     * @param publishTtl                     Publication ttl
     * @param publicationSteps               Publication steps for the publication period
     * @param publicationResolution          Publication resolution of the publication period
     * @param publishRetransmitCount         Number of publication retransmits
     * @param publishRetransmitIntervalSteps Publish retransmit interval steps
     * @param modelIdentifier                identifier for this model that will do publication
     * @throws IllegalArgumentException for invalid arguments
     * @deprecated in favour of {@link #ConfigModelPublicationSet(int, int, int, boolean, int, int, int, int, int, int)}
     */
    @Deprecated
    public ConfigModelPublicationSet(@NonNull final byte[] elementAddress,
                                     @NonNull final byte[] publishAddress,
                                     final int appKeyIndex,
                                     final boolean credentialFlag,
                                     final int publishTtl,
                                     final int publicationSteps,
                                     final int publicationResolution,
                                     final int publishRetransmitCount,
                                     final int publishRetransmitIntervalSteps,
                                     final int modelIdentifier) throws IllegalArgumentException {
        this(MeshParserUtils.bytesToInt(elementAddress),
                MeshParserUtils.bytesToInt(publishAddress),
                appKeyIndex,
                credentialFlag,
                publishTtl,
                publicationSteps,
                publicationResolution,
                publishRetransmitCount,
                publishRetransmitIntervalSteps,
                modelIdentifier);
    }

    /**
     * Constructs a ConfigModelPublicationSet message
     *
     * @param elementAddress                 Element address that should publish
     * @param publishAddress                 Address to which the element must publish
     * @param appKeyIndex                    Index of the application key
     * @param credentialFlag                 Credentials flag define which credentials to be used, set true to use friendship credentials and false for master credentials.
     *                                       Currently supports only master credentials
     * @param publishTtl                     Publication ttl
     * @param publicationSteps               Publication steps for the publication period
     * @param publicationResolution          Publication resolution of the publication period
     * @param publishRetransmitCount         Number of publication retransmits
     * @param publishRetransmitIntervalSteps Publish retransmit interval steps
     * @param modelIdentifier                identifier for this model that will do publication
     * @throws IllegalArgumentException for invalid arguments
     */
    public ConfigModelPublicationSet(final int elementAddress,
                                     final int publishAddress,
                                     final int appKeyIndex,
                                     final boolean credentialFlag,
                                     final int publishTtl,
                                     final int publicationSteps,
                                     final int publicationResolution,
                                     final int publishRetransmitCount,
                                     final int publishRetransmitIntervalSteps,
                                     final int modelIdentifier) throws IllegalArgumentException {
        if (!MeshAddress.isValidUnicastAddress(elementAddress))
            throw new IllegalArgumentException("Invalid unicast address, unicast address must be a 16-bit value, and must range range from 0x0001 to 0x7FFF");
        this.elementAddress = elementAddress;
        if (!MeshAddress.isAddressInRange(publishAddress))
            throw new IllegalArgumentException("Invalid publish address, publish address must be a 16-bit value");
        this.publishAddress = publishAddress;

        this.credentialFlag = credentialFlag;
        this.publishTtl = publishTtl;
        this.publicationSteps = publicationSteps;
        this.publicationResolution = publicationResolution;
        this.publishRetransmitCount = publishRetransmitCount;
        this.publishRetransmitIntervalSteps = publishRetransmitIntervalSteps;
        this.modelIdentifier = modelIdentifier;
        this.appKeyIndex = appKeyIndex;
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }


    @Override
    void assembleMessageParameters() {
        final ByteBuffer paramsBuffer;
        final byte[] applicationKeyIndex = MeshParserUtils.addKeyIndexPadding(appKeyIndex);
        Log.v(TAG, "AppKeyIndex: " + appKeyIndex);
        Log.v(TAG, "Element address: " + MeshAddress.formatAddress(elementAddress, true));
        Log.v(TAG, "Publish address: " + MeshAddress.formatAddress(publishAddress, true));
        Log.v(TAG, "Publish ttl: " + publishTtl);
        Log.v(TAG, "Publish steps: " + publicationSteps);
        Log.v(TAG, "Publish resolution: " + publicationResolution);
        Log.v(TAG, "Retransmission count: " + publishRetransmitCount);
        Log.v(TAG, "Retransmission interval: " + publishRetransmitIntervalSteps);
        Log.v(TAG, "Model: " + MeshParserUtils.bytesToHex(AddressUtils.getUnicastAddressBytes(modelIdentifier), false));

        final int rfu = 0; // We ignore the rfu here
        final int octet5 = ((applicationKeyIndex[0] << 4)) | (credentialFlag ? 1 : 0);
        final int octet8 = (publishRetransmitCount << 5) | (publishRetransmitIntervalSteps & 0x1F);
        //We check if the model identifier value is within the range of a 16-bit value here. If it is then it is a sigmodel
        if (modelIdentifier >= Short.MIN_VALUE && modelIdentifier <= Short.MAX_VALUE) {
            paramsBuffer = ByteBuffer.allocate(SIG_MODEL_PUBLISH_SET_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.putShort((short) this.elementAddress);
            paramsBuffer.putShort((short) this.publishAddress);
            paramsBuffer.put(applicationKeyIndex[1]);
            paramsBuffer.put((byte) octet5);
            paramsBuffer.put((byte) publishTtl);
            paramsBuffer.put((byte) ((publicationResolution << 6) | publicationSteps));
            paramsBuffer.put((byte) octet8);
            paramsBuffer.putShort((short) modelIdentifier);
            mParameters = paramsBuffer.array();
        } else {
            paramsBuffer = ByteBuffer.allocate(VENDOR_MODEL_PUBLISH_SET_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.putShort((short) this.elementAddress);
            paramsBuffer.putShort((short) this.publishAddress);
            paramsBuffer.put(applicationKeyIndex[1]);
            paramsBuffer.put((byte) octet5);
            paramsBuffer.put((byte) publishTtl);
            paramsBuffer.put((byte) ((publicationResolution << 6) | publicationSteps));
            paramsBuffer.put((byte) octet8);
            final byte[] modelIdentifier = new byte[]{(byte) ((this.modelIdentifier >> 24) & 0xFF), (byte) ((this.modelIdentifier >> 16) & 0xFF), (byte) ((this.modelIdentifier >> 8) & 0xFF), (byte) (this.modelIdentifier & 0xFF)};
            paramsBuffer.put(modelIdentifier[1]);
            paramsBuffer.put(modelIdentifier[0]);
            paramsBuffer.put(modelIdentifier[3]);
            paramsBuffer.put(modelIdentifier[2]);
            mParameters = paramsBuffer.array();
        }
    }

    /**
     * Returns the element address to which the app key must be bound.
     *
     * @return element address
     */
    public int getElementAddress() {
        return elementAddress;
    }

    /**
     * Returns the publish address to which the model must publish to
     */
    public int getPublishAddress() {
        return publishAddress;
    }

    /**
     * Returns the global index of the app key to be used for publication.
     *
     * @return app key index
     */
    public int getAppKeyIndex() {
        return appKeyIndex;
    }

    /**
     * Returns the credential flag to be used for this message.
     *
     * @return true if friendship credentials to be used or false if master credentials is to be used.
     */
    public boolean getCredentialFlag() {
        return credentialFlag;
    }

    /**
     * Returns the ttl of publication messages
     *
     * @return publication ttl
     */
    public int getPublishTtl() {
        return publishTtl;
    }

    /**
     * Returns the number of publication steps.
     *
     * @return number of steps
     */
    public int getPublicationSteps() {
        return publicationSteps;
    }

    /**
     * Returns the resolution for the publication steps.
     *
     * @return resolution
     */
    public int getPublicationResolution() {
        return publicationResolution;
    }

    /**
     * Returns the number of retransmissions for each published message.
     *
     * @return number of retransmits
     */
    public int getPublishRetransmitCount() {
        return publishRetransmitCount;
    }

    /**
     * Returns the number of 50-milliseconds steps between retransmissions.
     *
     * @return retransmit interval steps
     */
    public int getPublishRetransmitIntervalSteps() {
        return publishRetransmitIntervalSteps;
    }

    /**
     * Returns the model identifier to which the key is to be bound.
     *
     * @return 16-bit or 32-bit vendor model identifier
     */
    public int getModelIdentifier() {
        return modelIdentifier;
    }
}
