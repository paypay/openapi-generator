import { ResponseContext, RequestContext, HttpFile, HttpInfo } from '../http/http';
import { Configuration, ConfigurationOptions } from '../configuration'
import type { Middleware } from '../middleware';

import { Cat } from '../models/Cat';
import { Dog } from '../models/Dog';
import { FilePostRequest } from '../models/FilePostRequest';
import { PetByAge } from '../models/PetByAge';
import { PetByType } from '../models/PetByType';
import { PetsFilteredPatchRequest } from '../models/PetsFilteredPatchRequest';
import { PetsPatchRequest } from '../models/PetsPatchRequest';

import { ObservableDefaultApi } from "./ObservableAPI";
import { DefaultApiRequestFactory, DefaultApiResponseProcessor} from "../apis/DefaultApi";

export interface DefaultApiFilePostRequest {
    /**
     * 
     * @type FilePostRequest
     * @memberof DefaultApifilePost
     */
    filePostRequest?: FilePostRequest
}

export interface DefaultApiPetsFilteredPatchRequest {
    /**
     * 
     * @type PetsFilteredPatchRequest
     * @memberof DefaultApipetsFilteredPatch
     */
    petsFilteredPatchRequest?: PetsFilteredPatchRequest
}

export interface DefaultApiPetsPatchRequest {
    /**
     * 
     * @type PetsPatchRequest
     * @memberof DefaultApipetsPatch
     */
    petsPatchRequest?: PetsPatchRequest
}

export class ObjectDefaultApi {
    private api: ObservableDefaultApi

    public constructor(configuration: Configuration, requestFactory?: DefaultApiRequestFactory, responseProcessor?: DefaultApiResponseProcessor) {
        this.api = new ObservableDefaultApi(configuration, requestFactory, responseProcessor);
    }

    /**
     * @param param the request object
     */
    public filePostWithHttpInfo(param: DefaultApiFilePostRequest = {}, options?: ConfigurationOptions): Promise<HttpInfo<void>> {
        return this.api.filePostWithHttpInfo(param.filePostRequest,  options).toPromise();
    }

    /**
     * @param param the request object
     */
    public filePost(param: DefaultApiFilePostRequest = {}, options?: ConfigurationOptions): Promise<void> {
        return this.api.filePost(param.filePostRequest,  options).toPromise();
    }

    /**
     * @param param the request object
     */
    public petsFilteredPatchWithHttpInfo(param: DefaultApiPetsFilteredPatchRequest = {}, options?: ConfigurationOptions): Promise<HttpInfo<void>> {
        return this.api.petsFilteredPatchWithHttpInfo(param.petsFilteredPatchRequest,  options).toPromise();
    }

    /**
     * @param param the request object
     */
    public petsFilteredPatch(param: DefaultApiPetsFilteredPatchRequest = {}, options?: ConfigurationOptions): Promise<void> {
        return this.api.petsFilteredPatch(param.petsFilteredPatchRequest,  options).toPromise();
    }

    /**
     * @param param the request object
     */
    public petsPatchWithHttpInfo(param: DefaultApiPetsPatchRequest = {}, options?: ConfigurationOptions): Promise<HttpInfo<void>> {
        return this.api.petsPatchWithHttpInfo(param.petsPatchRequest,  options).toPromise();
    }

    /**
     * @param param the request object
     */
    public petsPatch(param: DefaultApiPetsPatchRequest = {}, options?: ConfigurationOptions): Promise<void> {
        return this.api.petsPatch(param.petsPatchRequest,  options).toPromise();
    }

}
